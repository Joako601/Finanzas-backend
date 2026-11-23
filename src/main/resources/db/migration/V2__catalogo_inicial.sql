-- Catálogo inicial de 12 productos financieros públicos de referencia en México.
-- Alineado 1:1 con el contrato ProductoFinanciero de :feature:nube en Android.

INSERT INTO producto_financiero (
    nombre, categoria, entidad, costo_referencia_pesos, periodicidad,
    tasa_anual_referencia, detalle_referencia, fuente_referencia, image_url, activo
) VALUES
(
    'BBVA Azul',
    'Tarjeta de crédito',
    'BBVA',
    748,
    'Anual',
    72.90,
    'CAT promedio 72.9% · Corte típico día 15',
    'Referencia pública CONDUSEF / BBVA',
    NULL,
    TRUE
),
(
    'Nu Crédito',
    'Tarjeta de crédito',
    'Nu México',
    0,
    'Anual',
    89.50,
    'Sin anualidad · Control de límite y fechas desde la app',
    'Referencia pública CONDUSEF / Nu México',
    NULL,
    TRUE
),
(
    'Citibanamex Clásica',
    'Tarjeta de crédito',
    'Banamex',
    815,
    'Anual',
    76.40,
    'CAT promedio 76.4% · Meses sin intereses y preventas',
    'Referencia pública CONDUSEF / Banamex',
    NULL,
    TRUE
),
(
    'Santander LikeU',
    'Tarjeta de crédito',
    'Santander',
    0,
    'Anual',
    68.20,
    'Sin anualidad con gasto mensual mínimo · Cashback en comercios',
    'Referencia pública CONDUSEF / Santander',
    NULL,
    TRUE
),
(
    'Cuenta Nu Débito',
    'Tarjeta de débito',
    'Nu México',
    0,
    'Sin costo',
    NULL,
    'Sin comisión por manejo de cuenta · Cajitas con rendimiento',
    'Referencia pública Nu México',
    NULL,
    TRUE
),
(
    'BBVA Libretón Básico Digital',
    'Tarjeta de débito',
    'BBVA',
    0,
    'Sin costo',
    NULL,
    'Sin saldo mínimo · Retiros sin tarjeta en cajeros',
    'Referencia pública CONDUSEF / BBVA',
    NULL,
    TRUE
),
(
    'Mercado Pago Débito',
    'Tarjeta de débito',
    'Mercado Pago',
    0,
    'Sin costo',
    NULL,
    'Sin comisión anual · Transferencias SPEI sin costo',
    'Referencia pública Mercado Pago',
    NULL,
    TRUE
),
(
    'CFE Doméstico',
    'Servicio',
    'CFE',
    450,
    'Bimestral',
    NULL,
    'Recibo bimestral estimado · Tarifa residencial básica',
    'Referencia pública CFE',
    NULL,
    TRUE
),
(
    'Telmex Infinitum',
    'Servicio',
    'Telmex',
    389,
    'Mensual',
    NULL,
    'Internet residencial fibra óptica · Pago mensual',
    'Referencia pública Telmex',
    NULL,
    TRUE
),
(
    'Telcel Amigo Sin Límite',
    'Servicio',
    'Telcel',
    200,
    'Mensual',
    NULL,
    'Telefonía móvil prepago · Vigencia típica de 30 días',
    'Referencia pública Telcel',
    NULL,
    TRUE
),
(
    'Spotify Individual',
    'Suscripción',
    'Spotify',
    129,
    'Mensual',
    NULL,
    'Música y podcasts sin anuncios · Cobro recurrente',
    'Referencia pública Spotify México',
    NULL,
    TRUE
),
(
    'Netflix Estándar con anuncios',
    'Suscripción',
    'Netflix',
    119,
    'Mensual',
    NULL,
    'Streaming Full HD en 2 dispositivos · Cobro recurrente',
    'Referencia pública Netflix México',
    NULL,
    TRUE
);
