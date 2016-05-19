(ns syng-im.group-settings.views.chat-name-edit
  (:require-macros [syng-im.utils.views :refer [defview]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view text-input]]
            [syng-im.components.toolbar :refer [toolbar]]
            [syng-im.group-settings.styles.chat-name-edit :as st]
            [syng-im.components.styles :refer [toolbar-background2
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
