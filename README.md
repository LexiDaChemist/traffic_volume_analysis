# Interstate Traffic Volume Analysis (Java)

This project analyzes interstate highway traffic volume using a structured Java
data pipeline. The goal is to extract meaningful temporal and contextual insights
from a real-world transportation dataset and present results through clear,
interpretable visualizations.

The project emphasizes data ingestion, aggregation, and visualization rather
than predictive modeling, reflecting a focus on analytical reasoning and software
engineering practices.

---

## Dataset

- **Name:** Interstate Traffic Dataset (US)
- **Source:** Kaggle
- **Creator:** Ansh Tanwar
- **Description:** Hourly traffic volume observations for a U.S. interstate
  highway, enriched with weather and temporal attributes.

> The dataset is not included in this repository.  
> Download it from Kaggle and place the CSV file in `data/raw/`.

---

## Project Objectives

- Load and validate a real-world CSV dataset in Java
- Model traffic observations using strongly-typed data structures
- Perform exploratory traffic volume analysis
- Identify temporal and contextual traffic patterns
- Generate publication-quality visualizations

---

## Key Results & Insights

### 1. Peak Traffic Hours (Commute Patterns)

Analysis of hourly traffic volume reveals a strong weekday commute signature.

**Findings:**
- Weekday traffic peaks during typical morning and evening commute hours
- Weekend traffic volumes are lower and more evenly distributed throughout the day
- This contrast highlights the role of structured work schedules in driving weekday demand

These patterns are consistent with expected commuter behavior on major interstate corridors.

---

### 2. Weekday vs Weekend Traffic Volume

![Weekday vs Weekend](reports/weekday_vs_weekend.jpg)

**Findings:**
- Average weekday traffic volume is significantly higher than weekend volume
- The difference reflects reduced commuter demand on weekends
- Numeric labels are included to support direct comparison

---

### 3. Weather Impact on Traffic Volume

Traffic volume was grouped by reported weather conditions.

**Findings:**
- Clear and cloudy conditions exhibit the highest average traffic volumes
- Rain and snow are associated with reduced traffic volume
- The results suggest weather sensitivity in travel behavior on the observed corridor

---

## Technical Approach

- **Language:** Java 17
- **Build System:** Maven
- **Libraries:**
  - Apache Commons CSV (data ingestion)
  - JFreeChart (visualization)

### Core Components
- `TrafficRecord` — immutable data model
- `TrafficDataLoader` — CSV parsing and validation
- `TrafficAnalysis` — aggregation and analysis logic
- `TrafficCharts` — chart generation and export
- `App` — application entry point

---

## Project Structure

```text
src/main/java/com/traffic/analysis/
  App.java
  TrafficRecord.java
  TrafficDataLoader.java
  TrafficAnalysis.java
  TrafficCharts.java

data/raw/
  .gitkeep

reports/
  weekday_vs_weekend.jpg

pom.xml

- Implement CSV data parsing in Java  
- Perform exploratory traffic volume analysis  
- Generate visualizations (line charts and bar charts)  
- Identify peak traffic periods and trends

