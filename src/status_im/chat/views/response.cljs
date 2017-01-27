(ns status-im.chat.views.response
  (:require-macros [reagent.ratom :refer [reaction]]
                   [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                animated-view
                                                icon
                                                image
                                                text
                                                touchable-highlight
                                                web-view
                                                scroll-view]]
            [status-im.components.drag-drop :as drag]
            [status-im.chat.styles.response :as st]
            [status-im.chat.styles.dragdown :as ddst]
            [status-im.components.animation :as anim]
            [status-im.chat.suggestions-responder :as resp]
            [status-im.chat.constants :as c]
            [status-im.chat.views.command-validation :as cv]
            [status-im.utils.platform :refer [ios?]]
            [status-im.components.webview-bridge :refer [webview-bridge]]
            [status-im.i18n :refer [label]]
            [status-im.accessibility-ids :as id]
            [status-im.utils.datetime :as dt]
            [status-im.utils.name :refer [shortened-name]]
            [status-im.utils.js-resources :as js-res]
            [status-im.commands.utils :as cu]))

(defn drag-icon []
  [view st/drag-container
   [icon :drag_white st/drag-icon]])

(defn command-icon [{icon-path :icon
                     color     :color}]
  [view st/command-icon-container
   (when icon-path
     [icon icon-path (st/command-icon color)])])

(defn request-info-text [name chat-id added]
  (let [name' (shortened-name (or name chat-id) 20)]
    (str "By " name' ", "
         (dt/format-date "MMM" added)
         " "
         (dt/get-ordinal-date added)
         " at "
         (dt/format-date "HH:mm" added))))

(defview info-container
  [command]
  [{:keys [name chat-id]} [:get-current-chat]
   {:keys [added]} [:get-current-request]]
  [view st/info-container
   [text {:style st/command-name}
    (str (:description command) " " (label :t/request))]
   (when added
     [text {:style st/message-info} (request-info-text name chat-id added)])])

(defn request-info [response-height]
  (let [layout-height (subscribe [:max-layout-height :default])
        pan-responder (resp/pan-responder response-height
                                          layout-height
                                          :fix-response-height)
        command       (subscribe [:get-chat-command])]
    (fn [_]
      (if (= :response (:type @command))
        [view (merge (drag/pan-handlers pan-responder)
                     {:style (st/request-info (:color @command))})
         [drag-icon]
         [view st/inner-container
          [command-icon @command]
          [info-container @command]
          [touchable-highlight {:on-press            #(dispatch [:start-cancel-command])
                                :accessibility-label id/chat-cancel-response-button}
           [view st/cancel-container
            [icon :close_white st/cancel-icon]]]]]
        [view (merge (drag/pan-handlers pan-responder)
                     {:style ddst/drag-down-touchable})
         [icon :drag_down ddst/drag-down-icon]]))))

(defn container-animation-logic [{:keys [to-value val animate?]}]
  (when-let [to-value @to-value]
    (let [max-layout-height (subscribe [:max-layout-height :default])
          to-value          (min to-value (max 0 @max-layout-height))]
      (when-not (= to-value (.-_value val))
        (if (or (nil? @animate?) @animate?)
          (anim/start (anim/timing val {:toValue  to-value
                                        :duration 300}))
          (anim/set-value val to-value))))))

(defn container [response-height & _]
  (let [;; todo to-response-height, cur-response-height must be specific
        ;; for each chat
        to-response-height (subscribe [:response-height :default])
        input-margin       (subscribe [:input-margin])
        changed            (subscribe [:animations :response-height-changed])
        animate?           (subscribe [:animate?])
        context            {:to-value to-response-height
                            :val      response-height
                            :animate? animate?}
        on-update          #(container-animation-logic context)]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn [response-height & children]
         @to-response-height @changed
         (into [animated-view {:style (st/response-view
                                        response-height
                                        @input-margin)}]
               children))})))

(defn on-navigation-change
  [event]
  (let [{:strs [loading url]} (js->clj event)]
    (when-not (= "about:blank" url)
      (if loading
        (dispatch [:set-web-view-url url])
        (dispatch [:set-chat-command-content (str cu/command-prefix url)])))))

(defn web-view-error []
  (r/as-element
    [view {:justify-content :center
           :align-items     :center
           :flex-direction  :row}
     [text (label :t/web-view-error)]]))

(defview suggestions-web-view []
  [url [:web-view-url]
   extra-js [:web-view-extra-js]]
  (when url
    [webview-bridge
     {:ref                                   #(dispatch [:set-webview-bridge %])
      :on-bridge-message                     #(dispatch [:webview-bridge-message %])
      :source                                {:uri url}
      :render-error                          web-view-error
      :java-script-enabled                   true
      :injected-on-start-loading-java-script (str js-res/web3 js-res/web3-init)
      :injected-java-script                  (str js-res/webview-js extra-js)
      :bounces                               false
      :on-navigation-state-change            on-navigation-change
      :local-storage-enabled                 true}]))

(defn placeholder []
  [view st/input-placeholder])

(defview response-suggestions-view []
  [suggestions [:get-content-suggestions]]
  (when (seq suggestions) suggestions))

(defn response-view []
  (let [response-height (anim/create-value c/input-height)]
    [container response-height
     [request-info response-height]
     [suggestions-web-view]
     [response-suggestions-view]
     [cv/validation-messages]
     [placeholder]]))
