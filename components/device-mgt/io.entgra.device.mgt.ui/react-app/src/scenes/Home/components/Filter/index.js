import React from 'react';

import { Form, Input, Button } from 'antd';

class Filter extends React.Component {
  handleSubmit = e => {
    e.preventDefault();
    this.props.form.validateFields((err, values) => {
      if (!err) {
        Object.keys(values).forEach(
          key => values[key] === undefined && delete values[key],
        );
        this.props.callback({}, values);
      }
    });
  };

  render() {
    const { getFieldDecorator } = this.props.form;

    return (
      <Form layout="inline" onSubmit={this.handleSubmit}>
        {this.props.fields.map(field => (
          <Form.Item key={field.name}>
            {getFieldDecorator(field.name)(
              <Input placeholder={field.placeholder} />,
            )}
          </Form.Item>
        ))}
        <Form.Item>
          <Button htmlType="submit" shape="circle" icon="search" />
        </Form.Item>
      </Form>
    );
  }
}

export default Form.create({ name: 'horizontal_login' })(Filter);