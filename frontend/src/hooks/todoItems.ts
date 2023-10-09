import {useEffect, useState} from 'react';
import dayjs, {Dayjs} from 'dayjs';
import _ from 'underscore';

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
      .then(responseItems => {
        const items: TodoItem[] = responseItems.map((item: any) => ({
          ...item,
          createdAt: dayjs(item.createdAt)
        }));

        setItems(sortItems(items));
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

  const unmarkItemDone = (itemId: string) => {
    fetch(`/api/todo/unmark/${itemId}`, {
      method: 'PUT'
    })
      .then(response => {
        refreshItems();
      })
      .catch(err => {
        console.log(`Item unmarking error ${err}`);
      });
  };

  const sortItems = (items: TodoItem[]) => {
    const [done, notDone] = _.partition(items, item => item.done);
    done.sort(
      (item1, item2) => item1.createdAt.isBefore(item2.createdAt) ? 1 : -1
    );
    notDone.sort(
      (item1, item2) => item1.createdAt.isBefore(item2.createdAt) ? 1 : -1
    );

    return [...notDone, ...done];
  };

  useEffect(() => refreshItems(), []);

  return {
    items,
    itemsLoadingError,
    itemsLoaded,
    addItem,
    markItemDone,
    unmarkItemDone
  };
};
