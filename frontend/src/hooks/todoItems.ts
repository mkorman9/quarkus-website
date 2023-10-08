import {useEffect, useState} from 'react';
import dayjs, {Dayjs} from 'dayjs';

export type TodoItem = {
  content: string;
  createdAt: Dayjs;
};

export const useTodoItems = () => {
  const [items, setItems] = useState<TodoItem[]>([]);
  const [itemsLoadingError, setItemsLoadingError] = useState<unknown>(null);
  const [itemsLoaded, setItemsLoaded] = useState<boolean>(false);

  const refreshItems = () => {
    setItemsLoaded(false);

    fetch('/api/todo')
      .then(response => {
        return response.json();
      })
      .then(items => {
        setItems(items.map((item: any) => ({
          content: item.content,
          createdAt: dayjs(item.createdAt)
        })));
      })
      .catch(e => {
        console.log(`Items loading error ${e}`);
        setItemsLoadingError(e);
      })
      .finally(() => {
        setItemsLoaded(true);
      });
  };

  useEffect(() => refreshItems(), []);

  return {
    items,
    itemsLoadingError,
    itemsLoaded
  };
};
