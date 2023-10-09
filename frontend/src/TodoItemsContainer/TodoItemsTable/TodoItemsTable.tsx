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
        {items.map((item, i) => (
          <tr key={i}>
            <td className='items-table-td'>
              <input type='checkbox' checked={item.done} onChange={() => onItemCheck(item)}/>
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
