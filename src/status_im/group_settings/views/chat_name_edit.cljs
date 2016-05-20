(ns status-im.group-settings.views.chat-name-edit
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view text-input]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.group-settings.styles.chat-name-edit :as st]
            [status-im.components.styles :refer [toolbar-background2
                                               text2-color]]))

(defn save-group-chat-name []
  (dispatch [:set-chat-name])
  (dispatch [:navigate-back]))

(defn chat-name-edit-toolbar [chat-name]
  [toolbar {:background-color toolbar-background2
            :title           "Edit chat name"
            ;; TODO change to dark 'ok' icon
            :action          {:image   {:source {:uri :icon_ok}
                                        :style  st/save-action-icon}
                              :handler save-group-chat-name}}])

(defview chat-name-edit []
  [new-chat-name [:get :new-chat-name]]
  [view st/chat-name-container
   [chat-name-edit-toolbar]
   [text-input {:style                st/chat-name-input
                :autoFocus            true
                :placeholderTextColor text2-color
                :onChangeText         #(dispatch [:set-new-chat-name %])}
    new-chat-name]])
