import {useTodoItems} from "../hooks/todoItems";
import './TodoItemsTable.css';

const TodoItemsTable = () => {
  const {items, itemsLoaded} = useTodoItems();

  if (!itemsLoaded) {
    return <></>;
  }

  return (
    <table className='items-table'>
      <tbody>
      {items.map((item, i) => (
        <tr key={i}>
          <td className='items-table-td'>{item.content}</td>
          <td className='items-table-td'>{item.createdAt.format('YYYY-MM-DD HH:mm')}</td>
        </tr>
      ))}
      </tbody>
    </table>
  );
};

export default TodoItemsTable;
