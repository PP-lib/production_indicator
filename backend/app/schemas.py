from pydantic import BaseModel, Field
from datetime import datetime
from typing import Optional, List

class ProductionRecordCreate(BaseModel):
    operator_id: str = Field(..., examples=["op1"])  # from NFC
    item_code: str = Field(..., examples=["ITEM123"]) # from barcode
    quantity: int = Field(..., ge=1, le=100000)
    terminal_time: datetime

class ProductionRecordRead(BaseModel):
    id: int
    operator_id: str
    item_code: str
    quantity: int
    terminal_time: datetime
    server_time: datetime

    class Config:
        from_attributes = True

class DailySummary(BaseModel):
    date: str
    total_quantity: int

class OperatorSummaryItem(BaseModel):
    operator_id: str
    total_quantity: int

class HourlySummaryItem(BaseModel):
    hour: int
    total_quantity: int

class HourlySummary(BaseModel):
    date: str
    hours: List[HourlySummaryItem]
