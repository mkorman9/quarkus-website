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
        {items.filter(item => !item.done).map((item, i) => (
          <tr key={i}>
            <td className='items-table-td items-table-td-mark'>
              <input type='checkbox' checked={item.done} onChange={() => onItemCheck(item)}/>
            </td>
            <td className='items-table-td content'>{item.content}</td>
          </tr>
        ))}
        {items.filter(item => item.done).length > 0 &&
          <tr>
            <td colSpan={2}><hr /></td>
          </tr>
        }
        {items.filter(item => item.done).map((item, i) => (
          <tr key={i}>
            <td className='items-table-td items-table-td-mark'>
              <input type='checkbox' checked={item.done} onChange={() => onItemCheck(item)}/>
            </td>
            <td className='items-table-td content'>{item.content}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default TodoItemsTable;
