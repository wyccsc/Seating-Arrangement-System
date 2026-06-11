# Seating Arrangement System

**本機開發**

- Java 17、Maven
- Node.js、npm
- SQL Server（使用 Docker 單獨啟動）

---

## 方式一：Docker Compose 一鍵啟動

在專案根目錄執行：

```bash
# 可選：複製環境變數範本並修改密碼
copy .env.example .env

docker compose up -d --build  # 啟動

docker compose down            # 停止服務
docker compose down -v         # 停止並清除資料庫資料
```

啟動後：

| 服務 | 位址 |
|------|------|
| 前端 | http://localhost |
| 後端 API | http://localhost:8080 |
| SQL Server | localhost:1433 |

### 環境變數

可透過 `.env` 檔或環境變數設定：

| 變數 | 預設值 | 說明 |
|------|--------|------|
| `MSSQL_SA_PASSWORD` | `YourStrong!Passw0rd` | SQL Server SA 密碼（需符合強度要求） |

### 注意事項

- 若本機 **1433** 埠已被佔用，請先停止其他 SQL Server 容器，或修改 `docker-compose.yml` 的 port mapping。
- 首次啟動需等待 SQL Server healthcheck 通過，後端才會開始連線。

---

## 方式二：本機開發

### 1. 啟動資料庫

```bash
docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=YourStrong!Passw0rd" -p 1433:1433 --name sqlserver -d mcr.microsoft.com/mssql/server:2022-latest
```

首次使用請在 SQL Server 建立資料庫：

```bash
docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=YourStrong!Passw0rd" -p 1433:1433 --name sqlserver -d mcr.microsoft.com/mssql/server:2022-latest
```

```sql
CREATE DATABASE SeatingArrangement;
```

後端啟動時會透過 Flyway 自動建立資料表與初始資料。

### 2. 啟動後端

```bash
cd backend
mvn spring-boot:run
```

預設位址：`http://localhost:8080`

可透過環境變數覆寫連線設定（預設值見 `backend/src/main/resources/application.yml`）：

| 變數 | 預設值 |
|------|--------|
| `DB_URL` | `jdbc:sqlserver://localhost:1433;databaseName=SeatingArrangement;encrypt=true;trustServerCertificate=true` |
| `DB_USERNAME` | `sa` |
| `DB_PASSWORD` | `YourStrong!Passw0rd` |
| `SERVER_PORT` | `8080` |

### 3. 啟動前端

```bash
cd frontend
npm install   # 首次執行
npm run dev
```

開啟終端機顯示的網址（通常為 `http://127.0.0.1:5173`）。

---

## 操作說明

1. 選擇**樓層**，查看該層座位配置。
2. 從下拉選單選擇**員工**。
3. 點擊空座位，加入待指派清單；點擊已佔用座位，加入待清除清單。
4. 確認無誤後，點擊 **Submit** 送出變更。
5. 頁面下方 **Management** 區塊可新增或刪除員工、座位。
