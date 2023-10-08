import './TodoItemsTable.css';
import {TodoItem} from '../../hooks/todoItems';

export type TodoItemsTableProps = {
  items: TodoItem[];
  onMarkItem: (item: TodoItem) => void;
};

const TodoItemsTable = (props: TodoItemsTableProps) => {
  const {items, onMarkItem} = props;

  return (
    <table className='items-table'>
      <tbody>
        {items.map((item, i) => (
          <tr key={i}>
            <td className='items-table-td'>
              <input type='checkbox' checked={item.done} onChange={() => onMarkItem(item)}/>
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
