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
            <td className='items-table-td'>No items</td>
          </tr>
        </>}
        {items.map((item, i) => (
          <tr key={i}>
            <td className='items-table-td items-table-td-mark'>
              <input type='checkbox' checked={item.done} onChange={() => onItemCheck(item)}/>
            </td>
            <td className='items-table-td content'>{item.content}</td>
            <td className='items-table-td items-table-td-timestamp'>{item.createdAt.format('YYYY-MM-DD HH:mm')}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default TodoItemsTable;
