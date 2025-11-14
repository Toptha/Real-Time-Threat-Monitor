# 🛡️ CTI Sentinel – Cyber Threat Intelligence Simulation & Analytics System

## ⭐ PROJECT OVERVIEW (Full Description)

**CTI Sentinel** is a multi-module cyber-intelligence simulation system built in **Java (Advanced Java)** that demonstrates how security monitoring tools detect and react to abnormal system behaviour.  
The project progressively implements various advanced Java concepts — **generics, Swing UI, collections, system monitoring, interfaces, multithreading**, and more — across weekly modules.

The system models:

- **Digital nodes** (like mini computers)
- **Attackers launching different cyber-attacks**
- **A monitoring engine that simulates real-time system impact**

It also integrates **real operating system metrics** like CPU usage and memory usage to make the simulation feel realistic, similar to how tools like **Splunk, Nagios, or CrowdStrike** visualize system stress.

The final output is a working **CTI Simulator dashboard**, an interactive UI that displays:
- Nodes under attack  
- CPU & Memory spikes  
- Behaviour simulation  
- Attacker logs  
- Automatic attack mode  
- Threat severity levels  

---

## ⭐ WHY THIS PROJECT STANDS OUT

Unlike basic Java CRUD or management systems, this project:

- Includes **real system metrics** (CPU & RAM)
- Features **interactive Swing visualizations** with progress bars and live attacks
- Simulates cyber attacks (CPU overload, Memory fill, Port flood, Anomaly spikes)
- Uses timers, events, multi-node UI, and automatic attack spawning
- Implements **Generics, Interfaces, Custom Classes, Swing, and Java System APIs**
- Looks like a simplified version of a **SOC (Security Operations Center) tool**
- Visually exciting AND technically impressive

---

## ⭐ MODULE-WISE SYSTEM BREAKDOWN

---

### 🔥 Module 1: Core Components & Simulation Engine

This module builds the foundation of CTI Sentinel.  
It contains three major classes inside one package (`cti`):

---

### **1. SystemStatsReader**
- Reads real CPU and memory usage using `OperatingSystemMXBean`
- Used to compare real system load vs simulated attacks
- Outputs: CPU%, Memory%, system load

---

### **2. Attacker**
A domain class representing a cyber attacker event.

Supports **4 built-in attack types**:

- `CPU_OVERLOAD`
- `MEM_FILL`
- `PORT_FLOOD`
- `ANOMALY_SPIKE`

Each attack stores:

- Attacker ID  
- Attacker IP  
- Attack Type  
- Intensity (1–10)  
- Timestamp  

---

### **3. SimulatorSwing**
An interactive Swing-based attack simulation tool:

- Displays multiple nodes, each showing:
  - CPU bar  
  - Memory bar  
  - Per-node attack logs
- Allows:
  - Manual attack selection (node + type + intensity)
  - Auto-attack mode (random attacks every ~1s)
- Node reactions:
  - Progress bar increases
  - Smooth decay animations
  - Critical alert highlights

---

### **MainApp.java**
A launcher menu (outside the package) using switch-case to run:

- System Stats  
- Attacker Console Demo  
- UI Simulator  

---

### **Module 1 Completes Requirements For:**

- Domain classes  
- Swing UI  
- Real OS monitoring  
- Simulation engine  
- Foundation for Generics in Module 2  

---

## ⭐ 🔥 Module 2 (Planned): Generic Data Processing System

Module 2 introduces **Generics** with CTI domain objects.

### Key components:

#### ✔ `GenericList<T>` / `GenericManager<T>`
Add/remove/display different CTI entities.

#### ✔ `GenericProcessor<T>`
- Average severity  
- Filter extreme attacks  
- Validate threat data  

#### ✔ Generic Method  
Print any collection (nodes, logs, attacks, metrics).

#### ✔ Generic Interface: `Evaluator<T>`
Implement conditions like:

- High CPU attacks  
- High severity threats  
- Repeated attackers  

---

## ⭐ 🔥 Module 3 (Planned): Mini Threat Intelligence Analytics

This module builds small analytical tools:

- Threat heatmap numbers  
- Attack severity classification  
- Node risk scoring  
- Log indexing using HashMaps & ArrayLists  
- Thread-based attack simulation  

---

## ⭐ 🔥 Module 4 (Planned): Extended Features

Showcases advanced Java concepts:

- Background threads  
- Swing UI enhancements  
- Export logs to CSV  
- Timer-based alerting (flashing nodes, sounds)  
- Optional JavaFX heatmap  

---

## ⭐ FULL PROJECT PIPELINE

```
ATTACKER (input) 
      ↓
SIMULATION ENGINE (Module 1)
      ↓
NODE REACTION MODEL (Module 1)
      ↓
GENERIC ANALYTICS (Module 2)
      ↓
THREAT EVALUATORS (Module 2)
      ↓
SECURITY DASHBOARD (Swing UI)
      ↓
LOGGING + REPORT (Modules 3 & 4)
```

---

## ⭐ PROJECT OUTCOME

By the end of the course, **CTI Sentinel** becomes:

> A fully interactive cybersecurity monitoring tool built purely in Java,  
> simulating live cyber attacks on multiple nodes,  
> and applying generics, interfaces, Swing, timers, domain design,  
> and real system APIs.

