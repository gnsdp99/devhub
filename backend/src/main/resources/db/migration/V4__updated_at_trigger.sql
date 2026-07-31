create function set_updated_at() returns trigger as
$$
begin
    new.updated_at = now();
    return new;
end;
$$ language plpgsql;

create trigger source_set_updated_at
    before update
    on source
    for each row
execute function set_updated_at();

create trigger feed_set_updated_at
    before update
    on feed
    for each row
execute function set_updated_at();