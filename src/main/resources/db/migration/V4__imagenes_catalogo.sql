-- Imágenes del catálogo de ejemplo (V2), alojadas en el bucket público "productos" de Supabase Storage.
-- Son ilustraciones propias sin logos ni marcas (fuente en docs/imagenes-catalogo/).
-- Solo llena los productos sin imagen para no pisar cambios hechos desde la API de administración.

UPDATE producto_financiero SET image_url = 'https://gqajsktcgxmavmgnthot.supabase.co/storage/v1/object/public/productos/bbva-azul.png' WHERE nombre = 'BBVA Azul' AND image_url IS NULL;
UPDATE producto_financiero SET image_url = 'https://gqajsktcgxmavmgnthot.supabase.co/storage/v1/object/public/productos/nu-credito.png' WHERE nombre = 'Nu Crédito' AND image_url IS NULL;
UPDATE producto_financiero SET image_url = 'https://gqajsktcgxmavmgnthot.supabase.co/storage/v1/object/public/productos/citibanamex-clasica.png' WHERE nombre = 'Citibanamex Clásica' AND image_url IS NULL;
UPDATE producto_financiero SET image_url = 'https://gqajsktcgxmavmgnthot.supabase.co/storage/v1/object/public/productos/santander-likeu.png' WHERE nombre = 'Santander LikeU' AND image_url IS NULL;
UPDATE producto_financiero SET image_url = 'https://gqajsktcgxmavmgnthot.supabase.co/storage/v1/object/public/productos/nu-debito.png' WHERE nombre = 'Cuenta Nu Débito' AND image_url IS NULL;
UPDATE producto_financiero SET image_url = 'https://gqajsktcgxmavmgnthot.supabase.co/storage/v1/object/public/productos/bbva-libreton.png' WHERE nombre = 'BBVA Libretón Básico Digital' AND image_url IS NULL;
UPDATE producto_financiero SET image_url = 'https://gqajsktcgxmavmgnthot.supabase.co/storage/v1/object/public/productos/mercado-pago-debito.png' WHERE nombre = 'Mercado Pago Débito' AND image_url IS NULL;
UPDATE producto_financiero SET image_url = 'https://gqajsktcgxmavmgnthot.supabase.co/storage/v1/object/public/productos/cfe-domestico.png' WHERE nombre = 'CFE Doméstico' AND image_url IS NULL;
UPDATE producto_financiero SET image_url = 'https://gqajsktcgxmavmgnthot.supabase.co/storage/v1/object/public/productos/telmex-infinitum.png' WHERE nombre = 'Telmex Infinitum' AND image_url IS NULL;
UPDATE producto_financiero SET image_url = 'https://gqajsktcgxmavmgnthot.supabase.co/storage/v1/object/public/productos/telcel-amigo.png' WHERE nombre = 'Telcel Amigo Sin Límite' AND image_url IS NULL;
UPDATE producto_financiero SET image_url = 'https://gqajsktcgxmavmgnthot.supabase.co/storage/v1/object/public/productos/spotify-individual.png' WHERE nombre = 'Spotify Individual' AND image_url IS NULL;
UPDATE producto_financiero SET image_url = 'https://gqajsktcgxmavmgnthot.supabase.co/storage/v1/object/public/productos/netflix-estandar.png' WHERE nombre = 'Netflix Estándar con anuncios' AND image_url IS NULL;
