import matplotlib.pyplot as plt
from matplotlib.ticker import LogLocator, FuncFormatter
import math
from matplotlib.ticker import ScalarFormatter

def read_pressure_file(filename, start_time):
    """Reads a pressure file and calculates the average pressure starting from a given time."""
    pressures = []
    
    with open(filename, 'r') as f:
        for line in f:
            time, pressure = map(float, line.strip().split())
            if time >= start_time:
                pressures.append(pressure)
    
    # Calculate the average pressure
    if pressures:
        average_pressure = sum(pressures) / len(pressures)
    else:
        average_pressure = 0  # Handle case where no data is after start_time
    
    return average_pressure

def calculate_temperature(v0):
    """Calculates the relative temperature of the system based on the initial velocity."""
    return v0**2  # T ~ v^2

def plot_pressure_vs_temperature(temperatures, pressures, ylabel):
    """Plots average pressure vs. temperature."""
    plt.figure(figsize=(10, 6))
    plt.plot(temperatures, pressures, marker='o', linestyle='-', color='b')
    plt.xlabel("Temperatura relativa (T ~ v^2)")
    plt.ylabel(f"{ylabel} (N/m)")
    plt.grid()
    ax = plt.gca()
    ax.ticklabel_format(style='sci', axis='y', scilimits=(0,0))  # Forzar notación científica
    ax.yaxis.set_major_formatter(ScalarFormatter(useMathText=True))  # Usar MathText para que muestre 10^x

    plt.tight_layout()
    plt.show()

if __name__ == "__main__":
    # Define the initial velocities for the three simulations
    initial_velocities = [3, 6, 10]
    
    # Define the starting time for the stationary state
    stationary_start_time = 0.025  # seconds
    
    # File names for each simulation
    simulations = [
        {"wall": "wall_pressure_v3.txt", "obstacle": "obstacle_pressure_v3.txt", "v0": 3},
        {"wall": "wall_pressure_v6.txt", "obstacle": "obstacle_pressure_v6.txt", "v0": 6},
        {"wall": "wall_pressure_v10.txt", "obstacle": "obstacle_pressure_v10.txt", "v0": 10},
    ]
    
    average_wall_pressures = []
    average_obstacle_pressures = []
    temperatures = []
    
    for sim in simulations:
        # Read and calculate average pressures for wall and obstacle
        avg_wall_pressure = read_pressure_file(sim["wall"], stationary_start_time)
        avg_obstacle_pressure = read_pressure_file(sim["obstacle"], stationary_start_time)
        
        # Append the average pressures
        average_wall_pressures.append(avg_wall_pressure)
        average_obstacle_pressures.append(avg_obstacle_pressure)
        
        # Calculate the relative temperature
        temperature = calculate_temperature(sim["v0"])
        temperatures.append(temperature)
    
    # Plot average wall pressure vs. temperature
    plot_pressure_vs_temperature(temperatures, average_wall_pressures, "Presión promedio en las paredes")
    
    # Plot average obstacle pressure vs. temperature
    plot_pressure_vs_temperature(temperatures, average_obstacle_pressures, "Presión promedio en el obstáculo")