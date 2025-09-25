from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from datetime import date
from typing import List

from ..database import get_db, Base, engine
from .. import crud, schemas, models

# Create tables on import (PoC simplicity; move to Alembic later)
Base.metadata.create_all(bind=engine)

router = APIRouter(prefix="/records", tags=["records"])

@router.post("", response_model=schemas.ProductionRecordRead)
def create_record(record_in: schemas.ProductionRecordCreate, db: Session = Depends(get_db)):
    try:
        record = crud.create_production_record(db, record_in)
        return record
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@router.get("/daily", response_model=schemas.DailySummary)
def daily_summary(date: date, db: Session = Depends(get_db)):
    total = crud.get_daily_total(db, date)
    return schemas.DailySummary(date=str(date), total_quantity=total)

@router.get("/operators/{operator_id}", response_model=schemas.OperatorSummaryItem)
def operator_summary(operator_id: str, date: date, db: Session = Depends(get_db)):
    total = crud.get_operator_summary(db, date, operator_id)
    return schemas.OperatorSummaryItem(operator_id=operator_id, total_quantity=total)

@router.get("/hourly", response_model=schemas.HourlySummary)
def hourly_summary(date: date, db: Session = Depends(get_db)):
    rows = crud.get_hourly_summary(db, date)
    items = [schemas.HourlySummaryItem(hour=h, total_quantity=q) for h, q in rows]
    return schemas.HourlySummary(date=str(date), hours=items)
