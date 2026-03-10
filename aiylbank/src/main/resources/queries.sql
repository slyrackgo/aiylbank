-- Найти ТОП-5 счетов по количеству транзакций за последний месяц

select a.id,
       a.number,
       count(*) as tx_count
from (
         select t.from_account_id as account_id, t.created_at
         from transfers t
         union all
         select t.to_account_id as account_id, t.created_at
         from transfers t
     ) all_ops
         join accounts a on a.id = all_ops.account_id
where all_ops.created_at >= now() - interval '1 month'
group by a.id, a.number
order by tx_count desc
limit 5;

-- Посчитать общую сумму переводов за выбранный период

select coalesce(sum(t.amount), 0) as total_amount
from transfers t
where t.created_at between :from_date and :to_date;

-- Найти счета с отрицательным балансом
select *
from accounts a
where a.balance < 0;

-- Оптимизация: добавляем индекс по дате для ускорения выборок по периоду

create index if not exists idx_transfers_created_at on transfers (created_at);

