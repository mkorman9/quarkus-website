import {useEffect, useState} from 'react';
import dayjs, {Dayjs} from 'dayjs';

export type TodoItem = {
  id: string;
  content: string;
  done: boolean;
  createdAt: Dayjs;
};

export type TodoItemsPage = {
  items: TodoItem[];
  nextPageToken: string;
};

export const useTodoItems = () => {
  const [items, setItems] = useState<TodoItem[]>([]);
  const [itemsLoadingError, setItemsLoadingError] = useState<unknown>(null);
  const [itemsLoaded, setItemsLoaded] = useState<boolean>(false);

  const refreshItems = () => {
    setItemsLoaded(false);
    setItemsLoadingError(null);

    fetch('/api/todo')
      .then(response => {
        return response.json();
      })
      .then(response => {
        const page: TodoItemsPage = response;
        const items: TodoItem[] = page.items.map(item => ({
          ...item,
          createdAt: dayjs(item.createdAt)
        }));

        setItems(items);
      })
      .catch(err => {
        console.error(err);
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
        console.error(err);
      });
  };

  const markItemDone = (itemId: string) => {
    fetch(`/api/todo/mark/${itemId}`, {
      method: 'POST'
    })
      .catch(err => {
        console.error(err);
      });

    setItems(items.map(item => {
      if (item.id === itemId) {
        item.done = true;
      }

      return item;
    }));
  };

  const unmarkItemDone = (itemId: string) => {
    fetch(`/api/todo/unmark/${itemId}`, {
      method: 'POST'
    })
      .catch(err => {
        console.error(err);
      });

    setItems(items.map(item => {
      if (item.id === itemId) {
        item.done = false;
      }

      return item;
    }));
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
