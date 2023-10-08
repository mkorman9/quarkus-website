import {useEffect, useState} from 'react';
import dayjs, {Dayjs} from 'dayjs';

export type TodoItem = {
  id: string;
  content: string;
  done: boolean;
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
          id: item.id,
          content: item.content,
          done: item.done,
          createdAt: dayjs(item.createdAt)
        })));
      })
      .catch(err => {
        console.log(`Items loading error ${err}`);
        setItemsLoadingError(err);
      })
      .finally(() => {
        setItemsLoaded(true);
      });
  };

  const addItem = (content: string) => {
    fetch('/api/todo', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        content: content
      })
    })
      .then(response => {
        refreshItems();
      })
      .catch(err => {
        console.log(`Item adding error ${err}`);
      });
  };

  const markItemDone = (itemId: string) => {
    fetch(`/api/todo/mark/${itemId}`, {
      method: 'PUT'
    })
      .then(response => {
        refreshItems();
      })
      .catch(err => {
        console.log(`Item marking error ${err}`);
      });
  };

  useEffect(() => refreshItems(), []);

  return {
    items,
    itemsLoadingError,
    itemsLoaded,
    addItem,
    markItemDone
  };
};
