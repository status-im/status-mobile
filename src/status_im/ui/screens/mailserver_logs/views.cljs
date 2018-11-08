(ns status-im.ui.screens.mailserver-logs.views
  (:require [status-im.ui.components.react :as react]
            [status-im.mailserver.core :as mailserver]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.components.list.views :as list]
            [status-im.utils.datetime :as datetime]
            [clojure.string :as string]))

(defn render-row
  [{:keys [timestamp event]}]
  (let [[event-name event-body] event]
    [react/touchable-highlight
     {:on-press #(.share
                  react/sharing
                  (clj->js
                   {:message (string/join " " [(datetime/timestamp->long-date timestamp)
                                               event-name
                                               (prn-str event-body)])}))}
     [react/view {:flex-direction     :column
                  :background-color   :white
                  :align-items        :center
                  :padding-horizontal 16}
      [react/text (datetime/timestamp->long-date timestamp)]
      [react/text event-name]
      [react/text (prn-str event-body)]]]))

(defn mailserver-logs []
  [react/view {:flex 1}
   [status-bar/status-bar]
   [toolbar/toolbar {}
    toolbar/default-nav-back
    [toolbar/content-title "Mailserver logs"]]
   [react/scroll-view
    [react/text {:on-press #(.share
                             react/sharing
                             (clj->js
                              {:message @mailserver/enode}))
                 :style    {:font-size   22
                            :font-weight :bold}}
     "Enode id (press to share)"]
    [react/text {:on-press #(.share
                             react/sharing
                             (clj->js
                              {:message @mailserver/enode}))}
     @mailserver/enode]
    [react/text {:on-press #(.share
                             react/sharing
                             (clj->js
                              {:message (mailserver/print-logs)}))
                 :style    {:font-size   22
                            :font-weight :bold}}
     "Logs (press to share)"]
    [list/flat-list {:data               @mailserver/logs
                     :default-separator? true
                     :key-fn             :timestamp
                     :render-fn          render-row
                     :style              {:padding-bottom 20}}]]])
