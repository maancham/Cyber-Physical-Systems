
import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt

sync = pd.DataFrame(
    {
        "Distance" : [0.5,5,10],
        "Sync Rate" : [0.87,0.63,0.61]
    }
)


drop = pd.DataFrame(
    {
        "Distance" : [0.5,5,10],
        "Drop Rate" : [1-0.8581,1-0.7294,1-0.7096]
    }
)

delay = pd.DataFrame(
    {
        "Distance" : [0.5,5,10,0.5,5,10],
        "Delay" : [30.90,34.65,38.72,68,68,76.5],
        "type" : ['Avg']*3+['Max']*3
    }
)

sns.lineplot(data=sync,x="Distance",y="Sync Rate")
plt.savefig("sync.png")

plt.cla()

sns.lineplot(data=drop,x="Distance",y="Drop Rate")
plt.savefig("drop.png")

plt.cla()

sns.lineplot(data=delay,x="Distance",y="Delay",hue="type")
plt.savefig("delay.png")
