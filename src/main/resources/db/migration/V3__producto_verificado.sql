-- Marca explícita de producto revisado contra la fuente oficial.
-- Reemplaza la heurística de buscar "sin verificar" en fuente_referencia.
-- Los productos existentes (incluido el catálogo de ejemplo de V2) quedan sin verificar.

ALTER TABLE producto_financiero
    ADD COLUMN verificado BOOLEAN NOT NULL DEFAULT FALSE;
