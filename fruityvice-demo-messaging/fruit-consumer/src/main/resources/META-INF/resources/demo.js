function init() {
	return {
		votes: [],
		votesCounter: {},
		baseUrl: '/fruits',
		chart: null,

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
		}
	}
}

function drawChart(chart, labels, data) {
	if (!chart) {
        const ctx = document.getElementById('votes-chart');
        return new Chart(ctx, {
            type: 'pie',
            data: {
              labels,
              datasets: [{
                label: 'Most voted fruits',
                data: data
              }]
            }
        });
    } else {
        chart.data.labels = [];
        chart.data.labels.push(...labels);
        chart.data.datasets[0].data = [];
        chart.data.datasets[0].label = 'Most voted fruits';
        chart.data.datasets[0].data.push(...data);
        console.log(chart.data.datasets[0].data);
        console.log(chart.data);
        chart.update();
        return chart;
    }
}