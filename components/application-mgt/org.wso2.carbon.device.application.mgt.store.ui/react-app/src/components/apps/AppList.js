import React from "react";
import AppCard from "./AppCard";
import {Col, message, Row} from "antd";
import axios from "axios";
import config from "../../../public/conf/config.json";

class AppList extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            apps: [],
            loading: false
        }
    }

    componentDidMount() {
        console.log("mounted");
        const {deviceType} = this.props;
        console.log(this.props);
        this.props.changeSelectedMenuItem(deviceType);
        this.fetchData(deviceType);
    }


    componentDidUpdate(prevProps, prevState) {
        if (prevProps.deviceType !== this.props.deviceType) {
            const {deviceType} = this.props;
            this.props.changeSelectedMenuItem(deviceType);
            this.fetchData(deviceType);
        }
    }

    fetchData = (deviceType) => {

        const payload = {};
        if(deviceType==="web-clip"){
            payload.appType= "WEB_CLIP";
        }else{
            payload.deviceType= deviceType;
        }
        const parameters = {
            method: "post",
            'content-type': "application/json",
            payload: JSON.stringify(payload),
            'api-endpoint': "/application-mgt-store/v1.0/applications/"
        };

        //url-encode parameters
        const request = Object.keys(parameters).map(key => key + '=' + parameters[key]).join('&');

        //send request to the invoker
        axios.post(config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri, request
        ).then(res => {
            if (res.status === 200) {
                //todo remove this property check after backend improvement
                let apps = (res.data.data.hasOwnProperty("applications")) ? res.data.data.applications : [];
                this.setState({
                    apps: apps,
                    loading: false
                })
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                //todo display a popup with error
                message.error('You are not logged in');
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/store/login';
            } else {
                message.error('Something went wrong... :(');
            }

            this.setState({loading: false});
        });
    };

    render() {
        const {apps} = this.state;

        return (
            <Row gutter={16}>
                {apps.map(app => (
                    <Col key={app.id} xs={12} sm={6} md={6} lg={4} xl={3}>
                        <AppCard key={app.id}
                                 app={app}
                        />
                    </Col>
                ))}
            </Row>
        );
    }
}

export default AppList;