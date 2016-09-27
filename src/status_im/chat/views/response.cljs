(ns status-im.chat.views.response
  (:require-macros [reagent.ratom :refer [reaction]]
                   [status-im.utils.views :refer [defview]]
                   [status-im.utils.slurp :refer [slurp]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                animated-view
                                                icon
                                                text
                                                text-input
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
            [status-im.components.webview-bridge :refer [webview-bridge]]))

(defn drag-icon []
  [view st/drag-container
   [icon :drag_white st/drag-icon]])

(defn command-icon []
  [view st/command-icon-container
   ;; TODO stub data: command icon
   [icon :dollar_green st/command-icon]])

(defn info-container [command]
  [view st/info-container
   [text {:style st/command-name}
    (:description command)]
   [text {:style st/message-info}
    ;; TODO stub data: request message info
    "By ???, MMM 1st at HH:mm"]])

(defn request-info [response-height]
  (let [layout-height (subscribe [:get :layout-height])
        pan-responder (resp/pan-responder response-height
                                          layout-height
                                          :fix-response-height)
        command (subscribe [:get-chat-command])]
    (fn [response-height]
      (if (= :response (:type @command))
        [view (merge (drag/pan-handlers pan-responder)
                     {:style (st/request-info (:color @command))})
         [drag-icon]
         [view st/inner-container
          [command-icon nil]
          [info-container @command]
          [touchable-highlight {:on-press #(dispatch [:start-cancel-command])}
           [view st/cancel-container
            [icon :close_white st/cancel-icon]]]]]
        [view (merge (drag/pan-handlers pan-responder)
                     {:style ddst/drag-down-touchable})
         [icon :drag_down ddst/drag-down-icon]]))))

(defn container-animation-logic [{:keys [to-value val animate?]}]
  (when-let [to-value @to-value]
    (when-not (= to-value (.-_value val))
      (if (or (nil? @animate?) @animate?)
        (anim/start (anim/spring val {:toValue to-value}))
        (anim/set-value val to-value)))))

(defn container [response-height & children]
  (let [;; todo to-response-height, cur-response-height must be specific
        ;; for each chat
        to-response-height (subscribe [:response-height])
        changed            (subscribe [:animations :response-height-changed])
        animate?           (subscribe [:animate?])
        keyboard-height    (subscribe [:get :keyboard-height])
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
                                        (if ios? @keyboard-height 0)
                                        response-height)}]
               children))})))

(defn on-navigation-change
  [event]
  (let [{:strs [loading url]} (js->clj event)]
    (when-not (= "about:blank" url)
      (if loading
        (dispatch [:set-web-view-url url])
        (dispatch [:set-chat-command-content (str "c " url)])))))

(defview suggestions-web-view []
  [url [:web-view-url]]
  (when url
    [webview-bridge
     {:ref                        #(dispatch [:set-webview-bridge %])
      :on-bridge-message          #(dispatch [:webview-bridge-message %])
      :source                     {:uri url}
      :java-script-enabled        true
      :injected-java-script       (slurp "resources/webview.js")
      :style                      {:height 300}
      :on-navigation-state-change on-navigation-change}]))

(defview placeholder []
  [suggestions [:get-content-suggestions]]
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
