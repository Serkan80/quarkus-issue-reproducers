function init() {
	return {
		votes: [],
		fruits: [],
		votesCounter: {},
		baseUrl: '/fruits',
		chart: null,
		hasVoted: false,

		getFruits() {
			const options = { headers: {'Content-Type': 'application/json'} };

            fetch(this.baseUrl, options)
                .then(res => res.json())
                .then(json => {
                    this.fruits = json;
                });
		},

		get(path) {
            const options = { headers: {'Content-Type': 'application/json'} };

            fetch(`${this.baseUrl}${path}`, options)
                .then(res => res.json())
                .then(json => {
                    this.votes = json;
                    this.votes.sort((a, b) => b.count - a.count);
                    this.votes.forEach(vote => {
                        if (!this.votesCounter[vote.fruit]) {
                            this.votesCounter[vote.fruit] = 0;
                        }
                        this.votesCounter[vote.fruit] += vote.count;
                    });

                    const labels = Object.keys(this.votesCounter);
                    const values = Object.values(this.votesCounter);
					this.chart = drawChart(this.chart, labels, values);
					this.getFruits();
					this.sse();
                });
		},

		sse() {
			this.source = new EventSource(this.baseUrl + "/votes/stream");
            this.source.onmessage = (event) => {
                const vote = JSON.parse(event.data);

                if (!this.votesCounter[vote.fruitName]) {
                    this.votesCounter[vote.fruitName] = 0;
                }
                this.votesCounter[vote.fruitName]++;

                const labels = Object.keys(this.votesCounter);
                const values = Object.values(this.votesCounter);
				this.chart = drawChart(this.chart, labels, values);
            };
		},

		voteOnFruit(fruitId) {
		    const randomNumber = Math.floor(Math.random() * 1000) + 1;
		    const body = { fruitId: fruitId, voterId: `anonymous-${randomNumber}`, channel: 'WEB' };
			const options = { headers: {'Content-Type': 'application/json'}, method: 'POST', body:  JSON.stringify(body)};

            fetch('http://localhost:8080/fruits/votes', options)
	            .then(res => {
	                if (res.ok) {
	                    this.hasVoted = true;
	                }
	            });
		}
	}
}

function drawChart(chart, labels, data) {
	const ctx = document.getElementById('votes-chart');
	if (!ctx) {
		console.warn('[drawChart] Canvas element not found');
		return;
	}

	// Always clean up broken or stale chart
	if (chart && typeof chart.destroy === 'function' && !chart._destroyed) {
		console.warn('[drawChart] Destroying existing chart before recreate');
		chart.destroy();
	}

	// Create a new chart from scratch
	const newChart = new window.Chart(ctx, {
		type: 'pie',
		data: {
			labels,
			datasets: [{
				label: 'Most voted fruits',
				data: data
			}]
		},
		options: {
			responsive: true,
			maintainAspectRatio: false,
			plugins: {
				legend: {
					display: true,
					position: 'top'
				},
				title: {
					display: true,
					text: 'Fruit Votes'
				}
			}
		}
	});

	return newChart;
}
