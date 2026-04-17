# pcc_java

Java implementation of the PCC/ECC context constraint checking system, migrated from the Cangjie version in [pcc_cangjie](pcc_cangjie/README.md).

## Environment

- JDK 26
- Maven Wrapper (already included: [mvnw.cmd](mvnw.cmd), [mvnw](mvnw))

## Project Layout

- Java source: [src](src)
- Runtime data: [data](data)
- Logs/output: [log](log) or custom `--outdir`
- Maven config: [pom.xml](pom.xml)

## Build

Windows PowerShell:

```powershell
.\mvnw.cmd -DskipTests compile
```

Linux/macOS:

```bash
./mvnw -DskipTests compile
```

## Run (Online)

Example with smoke data:

```powershell
.\mvnw.cmd exec:java "-Dexec.args=--data data/smoke --freshness 2 --convert kernel --method ecc --outdir log --outfile smoke_online_java.log"
```

## Run (Offline)

### Cangjie command (reference)

```bash
cjpm run --run-args "--data data/data_with_link_oracles --context data/data_1/0-1.txt --pattern consistency_patterns_48.xml --constraint consistency_rules_48.xml --convert none --offline --outdir link_oracle/data_1 --outfile 0-1_answer_pcc.txt --method pcc"
```

### Java equivalent

```powershell
.\mvnw.cmd exec:java "-Dexec.args=--data data/data_with_link_oracles --context data/data_1/0-1.txt --pattern consistency_patterns_48.xml --constraint consistency_rules_48.xml --convert none --offline --outdir link_oracle/data_1 --outfile 0-1_answer_pcc.txt --method pcc"
```

## Common Arguments

- `--data`: base data directory (required)
- `--context`: offline context file path, relative to `--data`
- `--pattern`: pattern XML file, relative to `--data`
- `--constraint`: constraint XML file, relative to `--data`
- `--convert`: `kernel` | `notleaf` | `none`
- `--method`: `ecc` | `pcc`
- `--freshness`: freshness window in seconds (online mode)
- `--interval`: context injection interval in milliseconds (online mode)
- `--outdir`: output directory
- `--outfile`: output filename
- `--offline`: enable offline replay mode
- `--start_time`: optional online mock start timestamp (unix seconds)

## Quick Validation

Compile + run online smoke:

```powershell
.\mvnw.cmd -DskipTests compile; .\mvnw.cmd exec:java "-Dexec.args=--data data/smoke --freshness 2 --convert kernel --method ecc --outdir log --outfile smoke_online_java.log"
```

## Notes

- Link type output is lowercase to match the original convention: `violated` / `satisfied`.
- On JDK 26, Maven may print reflective/native access warnings. They do not block build/run.
