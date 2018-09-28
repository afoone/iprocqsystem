SELECT s.name, count(*) FROM qsystem.statistic st  join qsystem.services s on s.id = st.service_id group by s.id;

SELECT MONTH(st.user_start_time), count(*) FROM qsystem.statistic st  join qsystem.services s on s.id = st.service_id group by MONTH(st.user_start_time);

SELECT* FROM qsystem.statistic;