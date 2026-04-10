alter table repair_images
    add constraint uk_repair_images_shop_item unique (shop_id, repair_item_id);
