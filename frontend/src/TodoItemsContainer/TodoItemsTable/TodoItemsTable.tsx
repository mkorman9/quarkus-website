import './TodoItemsTable.css';
import {TodoItem} from '../../hooks/todoItems';

export type TodoItemsTableProps = {
  items: TodoItem[];
  onItemCheck: (item: TodoItem) => void;
};

const TodoItemsTable = (props: TodoItemsTableProps) => {
  const {items, onItemCheck} = props;

  return (
    <table className='items-table'>
      <tbody>
        {items.length === 0 && <>
          <tr>
            <td className='items-table-status-text'>No items</td>
          </tr>
        </>}
        {items.map((item, i) => (
          <tr key={i}>
            <td className='items-table-mark'>
              <input type='checkbox' checked={item.done} onChange={() => onItemCheck(item)}/>
            </td>
            <td className={item.done ? 'item-done' : ''}>{item.content}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default TodoItemsTable;
