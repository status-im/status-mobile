'use strict';

import React, {Component, PropTypes} from 'react';
import {requireNativeComponent} from 'react-native';
import {WebView} from 'react-native-webview';

export default class StatusWebView extends Component {
  static propTypes = WebView.propTypes;

  render() {
    return (
      <WebView {...this.props} nativeConfig={{component: RCTStatusWebView}} />
    );
  }
}

const RCTStatusWebView = requireNativeComponent(
  'StatusWebView',
  StatusWebView,
  WebView.extraNativeComponentConfig
);

import { NativeModules } from 'react-native';
module.exports = NativeModules.Status;
