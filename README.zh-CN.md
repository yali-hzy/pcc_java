# pcc_java（中文版）

这是从仓颉版本迁移而来的 Java 实现，用于执行 PCC/ECC 上下文约束检查。

- 仓颉参考实现见 [pcc_cangjie/README.md](pcc_cangjie/README.md)
- Java 版英文文档见 [README.md](README.md)

## 环境要求

- JDK 26
- Maven Wrapper（项目已包含 [mvnw.cmd](mvnw.cmd)、[mvnw](mvnw)）

## 目录结构

- Java 源码：[src](src)
- 运行数据：[data](data)
- 输出日志：[log](log) 或自定义 `--outdir`
- Maven 配置：[pom.xml](pom.xml)

## 编译

Windows PowerShell：

```powershell
.\mvnw.cmd -DskipTests compile
```

Linux/macOS：

```bash
./mvnw -DskipTests compile
```

## 在线模式运行示例

使用 smoke 数据：

```powershell
.\mvnw.cmd exec:java "-Dexec.args=--data data/smoke --freshness 2 --convert kernel --method ecc --outdir log --outfile smoke_online_java.log"
```

## 离线模式运行示例

### 仓颉命令（对照）

```bash
cjpm run --run-args "--data data/data_with_link_oracles --context data/data_1/0-1.txt --pattern consistency_patterns_48.xml --constraint consistency_rules_48.xml --convert none --offline --outdir link_oracle/data_1 --outfile 0-1_answer_pcc.txt --method pcc"
```

### Java 等价命令

```powershell
.\mvnw.cmd exec:java "-Dexec.args=--data data/data_with_link_oracles --context data/data_1/0-1.txt --pattern consistency_patterns_48.xml --constraint consistency_rules_48.xml --convert none --offline --outdir link_oracle/data_1 --outfile 0-1_answer_pcc.txt --method pcc"
```

## 常用参数

- `--data`：数据根目录（必需）
- `--context`：离线上下文文件路径（相对 `--data`）
- `--pattern`：模式 XML 路径（相对 `--data`）
- `--constraint`：约束 XML 路径（相对 `--data`）
- `--convert`：`kernel` | `notleaf` | `none`
- `--method`：`ecc` | `pcc`
- `--freshness`：新鲜度窗口（秒，在线模式）
- `--interval`：上下文注入间隔（毫秒，在线模式）
- `--outdir`：输出目录
- `--outfile`：输出文件名
- `--offline`：启用离线回放模式
- `--start_time`：在线模式可选起始时间戳（Unix 秒）

## 快速验证

编译并运行在线 smoke：

```powershell
.\mvnw.cmd -DskipTests compile; .\mvnw.cmd exec:java "-Dexec.args=--data data/smoke --freshness 2 --convert kernel --method ecc --outdir log --outfile smoke_online_java.log"
```

## 说明

- 结果中的链接类型已与原始约定保持一致，输出为小写：`violated` / `satisfied`。
- 在 JDK 26 下，Maven 可能出现反射/本地访问的 warning，这些警告不影响编译和运行。
