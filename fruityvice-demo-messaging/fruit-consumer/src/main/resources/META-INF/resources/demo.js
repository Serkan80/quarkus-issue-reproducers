function init() {
	return {
		fruits: [],
		fruitCounter: {},
		baseUrl: '/fruits',
		chart: null,

		get(path) {
            const options = { headers: {'Content-Type': 'application/json'} };

            fetch(`${this.baseUrl}${path}`, options)
                .then(res => res.json())
                .then(json => {
                    this.fruits = json;
                    this.fruits.sort((a, b) => b.counter - a.counter);
                    this.fruits.forEach(fruit => this.fruitCounter[fruit.name] = fruit.counter);

                    const labels = Object.keys(this.fruitCounter);
                    const values = Object.values(this.fruitCounter);
					this.chart = drawChart(this.chart, labels, values);
                });
		},

		sse() {
			this.source = new EventSource(this.baseUrl);
            this.source.onmessage = (event) => {
                const fruit = JSON.parse(event.data);
                this.fruitCounter[fruit.name] = (fruit.counter || 1);

                const labels = Object.keys(this.fruitCounter);
                const values = Object.values(this.fruitCounter);
				this.chart = drawChart(this.chart, labels, values);
            };
		}
	}
}

function drawChart(chart, labels, data) {
	if (!chart) {
        const ctx = document.getElementById('fruits-chart');
        return new Chart(ctx, {
            type: 'bar',
            data: {
              labels,
              datasets: [{
                label: 'Most voted fruits',
                data: data
              }]
            }
        });
    } else {
        chart.data.labels = labels;
        chart.data.datasets[0].data = data;
        chart.update();
        return chart;
    }
}