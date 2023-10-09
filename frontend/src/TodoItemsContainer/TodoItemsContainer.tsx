import {TodoItem, useTodoItems} from '../hooks/todoItems';
import TodoItemsTable from './TodoItemsTable/TodoItemsTable';
import TodoItemsAddForm from './TodoItemsAddForm/TodoItemsAddForm';

const TodoItemsContainer = () => {
  const {
    items,
    addItem,
    markItemDone,
    unmarkItemDone
  } = useTodoItems();

  const onAddItem = (content: string) => {
    addItem(content);
  };

  const onItemCheck = (item: TodoItem) => {
    if (!item.done) {
      markItemDone(item.id);
    } else {
      unmarkItemDone(item.id);
    }
  };

  return (
    <div>
      <TodoItemsAddForm onAddItem={onAddItem}/>
      <TodoItemsTable items={items} onItemCheck={onItemCheck}/>
    </div>
  );
};

export default TodoItemsContainer;
