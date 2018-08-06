-- -----------------------------------------------------
-- Update DB 3 to 4
-- -----------------------------------------------------


-- -----------------------------------------------------
-- Table `qsystem`.`users`
-- -----------------------------------------------------

ALTER TABLE `services` ADD `inputed_as_ext` TINYINT(1) NOT NULL DEFAULT false COMMENT 'Разрешение выводить введенные данные в третью колонку на табло и в панель вызова';
ALTER TABLE `services` ADD `tablo_text` VARCHAR(1500) NOT NULL DEFAULT '' COMMENT 'Текст для вывода на главное табло в шаблоны панели вызванного и третью колонку пользователя';

-- -----------------------------------------------------
-- Table `qsystem`.`services_langs`
-- -----------------------------------------------------

ALTER TABLE `services_langs` ADD `tablo_text` VARCHAR(1500) NOT NULL DEFAULT '';


-- -----------------------------------------------------
-- Table `qsystem`.`users`
-- -----------------------------------------------------

ALTER TABLE `users` ADD `tablo_text` VARCHAR(1500) NOT NULL DEFAULT '' COMMENT 'Текст для вывода на главное табло в шаблоны панели вызванного и третью колонку пользователя';

-- -----------------------------------------------------
-- Table  `net`
-- -----------------------------------------------------
UPDATE net SET version = '4' where id<>-1;

COMMIT;

