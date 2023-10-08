import {TodoItem, useTodoItems} from '../hooks/todoItems';
import TodoItemsTable from './TodoItemsTable/TodoItemsTable';
import TodoItemsAddForm from './TodoItemsAddForm/TodoItemsAddForm';

const TodoItemsContainer = () => {
  const {
    items,
    itemsLoaded,
    addItem,
    markItemDone,
    unmarkItemDone
  } = useTodoItems();

  const onAddItem = (content: string) => {
    addItem(content);
  };

  const onMarkItem = (item: TodoItem) => {
    if (!item.done) {
      markItemDone(item.id);
    } else {
      unmarkItemDone(item.id);
    }
  };

  if (!itemsLoaded) {
    return <></>;
  }

  return (
    <div>
      <TodoItemsAddForm onAddItem={onAddItem}/>
      <TodoItemsTable items={items} onMarkItem={onMarkItem}/>
    </div>
  );
};

export default TodoItemsContainer;
