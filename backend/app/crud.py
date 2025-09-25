from sqlalchemy.orm import Session
from sqlalchemy import select, func
from datetime import datetime, date
from . import models, schemas

def create_production_record(db: Session, record_in: schemas.ProductionRecordCreate) -> models.ProductionRecord:
    record = models.ProductionRecord(
        operator_id=record_in.operator_id,
        item_code=record_in.item_code,
        quantity=record_in.quantity,
        terminal_time=record_in.terminal_time,
    )
    db.add(record)
    db.commit()
    db.refresh(record)
    return record


def get_daily_total(db: Session, target_date: date):
    start = datetime.combine(target_date, datetime.min.time())
    end = datetime.combine(target_date, datetime.max.time())
    stmt = select(func.sum(models.ProductionRecord.quantity)).where(
        models.ProductionRecord.server_time >= start,
        models.ProductionRecord.server_time <= end,
    )
    total = db.execute(stmt).scalar() or 0
    return total


def get_operator_summary(db: Session, target_date: date, operator_id: str):
    start = datetime.combine(target_date, datetime.min.time())
    end = datetime.combine(target_date, datetime.max.time())
    stmt = select(func.sum(models.ProductionRecord.quantity)).where(
        models.ProductionRecord.operator_id == operator_id,
        models.ProductionRecord.server_time >= start,
        models.ProductionRecord.server_time <= end,
    )
    total = db.execute(stmt).scalar() or 0
    return total


def get_hourly_summary(db: Session, target_date: date):
    start = datetime.combine(target_date, datetime.min.time())
    end = datetime.combine(target_date, datetime.max.time())
    # SQLite: extract hour via strftime('%H', column)
    hour_expr = func.strftime('%H', models.ProductionRecord.server_time)
    stmt = select(hour_expr.label('hour'), func.sum(models.ProductionRecord.quantity)) \
        .where(models.ProductionRecord.server_time >= start, models.ProductionRecord.server_time <= end) \
        .group_by(hour_expr) \
        .order_by(hour_expr)
    result = db.execute(stmt).all()
    # Ensure 0-23 coverage
    hour_map = {int(row[0]): row[1] for row in result}
    return [(h, hour_map.get(h, 0)) for h in range(24)]
