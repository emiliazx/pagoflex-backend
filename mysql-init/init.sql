CREATE DATABASE IF NOT EXISTS pagoflex_usuarios CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS pagoflex_deudas CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS pagoflex_pagos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS pagoflex_tarjetas CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'pagoflex'@'%' IDENTIFIED BY 'pagoflex123';

GRANT ALL PRIVILEGES ON pagoflex_usuarios.* TO 'pagoflex'@'%';
GRANT ALL PRIVILEGES ON pagoflex_deudas.* TO 'pagoflex'@'%';
GRANT ALL PRIVILEGES ON pagoflex_pagos.* TO 'pagoflex'@'%';
GRANT ALL PRIVILEGES ON pagoflex_tarjetas.* TO 'pagoflex'@'%';

FLUSH PRIVILEGES;