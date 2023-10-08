import './TodoItemsTable.css';
import {TodoItem, useTodoItems} from '../hooks/todoItems';

const TodoItemsTable = () => {
  const {
    items,
    itemsLoaded,
    markItemDone,
    unmarkItemDone
  } = useTodoItems();

  const onMark = (item: TodoItem) => {
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
    <table className='items-table'>
      <tbody>
        {items.map((item, i) => (
          <tr key={i}>
            <td className='items-table-td'>
              <input type='checkbox' checked={item.done} onChange={() => onMark(item)}/>
            </td>
            <td className='items-table-td'>{item.content}</td>
            <td className='items-table-td'>{item.createdAt.format('YYYY-MM-DD HH:mm')}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default TodoItemsTable;
