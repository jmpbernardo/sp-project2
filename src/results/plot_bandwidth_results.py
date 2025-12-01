import pandas as pd
import matplotlib.pyplot as plt

file_alg1 = "bandwidth_results_alg1.csv"
file_alg2 = "bandwidth_results_alg2.csv"

def plot(number_of_circuits=50):
    df1 = pd.read_csv(file_alg1)
    df2 = pd.read_csv(file_alg2)

    df = pd.merge(df1, df2, on="circuit_id", suffixes=("_alg1", "_alg2"))
    df = df.sort_values("circuit_id")
    df = df.head(number_of_circuits)

    x = range(len(df))
    width = 0.2
    plt.figure(figsize=(10, 5))
    plt.bar(
        [i - width / 2 for i in x],
        df["min_bandwidth_alg1"],
        width,
        label="Alg 1",
    )
    plt.bar(
        [i + width / 2 for i in x],
        df["min_bandwidth_alg2"],
        width,
        label="Alg 2",
    )

    plt.xticks(x, df["circuit_id"])
    plt.xlabel("Circuit ID")
    plt.ylabel("Min Bandwidth")
    plt.legend()
    plt.tight_layout()
    plt.savefig("bandwidth_comparison.pdf")

if __name__ == "__main__":
    plot(number_of_circuits=40)
