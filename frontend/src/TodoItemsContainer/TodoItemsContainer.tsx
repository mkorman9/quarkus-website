import './TodoItemsContainer.css';
import {TodoItem, useTodoItems} from '../hooks/todoItems';
import TodoItemsTable from './TodoItemsTable/TodoItemsTable';
import TodoItemsAddForm from './TodoItemsAddForm/TodoItemsAddForm';

const TodoItemsContainer = () => {
  const {
    items,
    itemsLoadingError,
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

  if (itemsLoadingError) {
    return <span className='items-loading-error-text'>Items loading error!</span>;
  }

  return (
    <div>
      <TodoItemsAddForm onAddItem={onAddItem}/>
      <TodoItemsTable items={items} onItemCheck={onItemCheck}/>
    </div>
  );
};

export default TodoItemsContainer;
