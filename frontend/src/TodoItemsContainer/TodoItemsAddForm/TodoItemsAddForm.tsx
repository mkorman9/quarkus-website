import './TodoItemsAddForm.css';
import {ChangeEvent, KeyboardEvent, useState} from 'react';

export type TodoItemsAddFormProps = {
  onAddItem: (content: string) => void;
};

const TodoItemsAddForm = (props: TodoItemsAddFormProps) => {
  const {onAddItem} = props;

  const [content, setContent] = useState('');

  const onContentChange = (event: ChangeEvent<HTMLInputElement>) => {
    setContent(event.target.value);
  };

  const onAddButtonClick = () => {
    onAddItem(content);
    setContent('');
  };

  const onKeyDownOnContent = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      onAddButtonClick();
      return;
    }
  };

  return (
    <form className='add-item-form'>
      <input
        id='add-item-content'
        type='text'
        placeholder='Buy concert tickets...'
        value={content}
        onChange={onContentChange}
        onKeyDown={onKeyDownOnContent}
      />
      <button id='add-item-button' type='button' onClick={onAddButtonClick}>Add</button>
    </form>
  );
};

export default TodoItemsAddForm;
