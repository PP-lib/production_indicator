from fastapi import FastAPI
from .routers import records

app = FastAPI(title="Production Indicator API", version="0.1.0")

app.include_router(records.router)

@app.get("/")
async def root():
    return {"message": "Production Indicator API running"}
