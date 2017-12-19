(ns status-im.ui.components.toolbar.view
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [status-im.ui.components.react :as rn]
            [status-im.ui.components.sync-state.gradient :as sync-state-gradient-view]
            [status-im.ui.components.styles :as st]
            [status-im.ui.components.context-menu :as context-menu]
            [status-im.ui.components.toolbar.actions :as act]
            [status-im.ui.components.toolbar.styles :as tst]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.utils.platform :as platform]))

;; Navigation item

(defn nav-item
  [{:keys [handler accessibility-label style] :or {handler #(rf/dispatch [:navigate-back])}} item]
  [rn/touchable-highlight
   (merge {:on-press handler}
          (when accessibility-label
            {:accessibility-label accessibility-label}))
   [rn/view {:style style}
    item]])

(defn nav-button
  [{:keys [icon icon-opts] :as props}]
  [nav-item (merge {:style tst/nav-item-button} props)
   [vi/icon icon icon-opts]])

(defn nav-text
  ([text] (nav-text text nil))
  ([text handler] (nav-text nil text handler))
  ([props text handler]
   [rn/text (merge {:style (merge tst/item tst/item-text) :on-press (or handler #(rf/dispatch [:navigate-back]))})
    text]))

(defn nav-clear-text
  ([text] (nav-clear-text text nil))
  ([text handler]
   (nav-text tst/item-text-white-background text handler)))

(def default-nav-back [nav-button act/default-back])

;; Content

(defn content-wrapper [content]
  [rn/view {:style tst/toolbar-container}
   content])

(defn content-title
  ([title] (content-title nil title))
  ([title-style title]
   (content-title title-style title nil nil))
  ([title-style title subtitle-style subtitle]
   [rn/view {:style tst/toolbar-title-container}
    [rn/text {:style (merge tst/toolbar-title-text title-style)
              :font  :toolbar-title}
     title]
    (when subtitle [rn/text {:style subtitle-style} subtitle])]))

;; Actions

(defn text-action [{:keys [style handler disabled?]} title]
  [rn/text {:style    (merge tst/item tst/item-text style
                             (when disabled? tst/toolbar-text-action-disabled))
            :on-press (when-not disabled? handler)}
   title])

(def blank-action [rn/view {:style (merge tst/item tst/toolbar-action)}])

(defn- option-actions [icon icon-opts options]
  [context-menu/context-menu
   [rn/view {:style tst/toolbar-action}
    [vi/icon icon icon-opts]]
   options
   nil
   tst/item])

(defn- icon-action [icon {:keys [overlay-style] :as icon-opts} handler]
  [rn/touchable-highlight {:on-press handler}
   [rn/view {:style (merge tst/item tst/toolbar-action)}
    (when overlay-style
      [rn/view overlay-style])
    [vi/icon icon icon-opts]]])

(defn actions [v]
  [rn/view {:style tst/toolbar-actions}
   (for [{:keys [image icon icon-opts options handler]} v]
     (with-meta
       (cond (= image :blank)
             blank-action

             options
             [option-actions icon icon-opts options]

             :else
             [icon-action icon icon-opts handler])
       {:key (str "action-" (or image icon))}))])

(defn toolbar
  ([props nav-item content-item] (toolbar props nav-item content-item [actions [{:image :blank}]]))
  ([{:keys [background-color style flat? show-sync-bar?]}
    nav-item
    content-item
    action-items]
   ;; TODO remove extra view wen we remove sync-state-gradient
   [rn/view
    [rn/view {:style (merge (tst/toolbar background-color flat?) style)}
     ;; On iOS title must be centered. Current solution is a workaround and eventually this will be sorted out using flex
     (when platform/ios?
       [rn/view tst/ios-content-item
        content-item])
     (when nav-item
       [rn/view {:style (tst/toolbar-nav-actions-container 0)}
        nav-item])
     (if platform/ios?
       [rn/view st/flex]
       content-item)
     action-items]
    (when show-sync-bar? [sync-state-gradient-view/sync-state-gradient-view])]))

(defn simple-toolbar
  "A simple toolbar whose content is a single line text"
  ([] (simple-toolbar nil))
  ([title] (toolbar nil default-nav-back [content-title title])))

(def search-text-input (r/atom nil))

(defn- toolbar-search-submit [on-search-submit]
  (let [text @(rf/subscribe [:get-in [:toolbar-search :text]])]
    (on-search-submit text)
    (rf/dispatch [:set-in [:toolbar-search :text] nil])))

(defn- toolbar-with-search-content [{:keys [show-search?
                                            search-placeholder
                                            title
                                            custom-title
                                            on-search-submit]}]
  [rn/view tst/toolbar-with-search-content
   (if show-search?
     [rn/text-input
      {:style                  tst/toolbar-search-input
       :ref                    #(reset! search-text-input %)
       :auto-focus             true
       :placeholder            search-placeholder
       :placeholder-text-color st/color-gray4
       :on-change-text         #(rf/dispatch [:set-in [:toolbar-search :text] %])
       :on-submit-editing      (when on-search-submit
                                 #(toolbar-search-submit on-search-submit))}]
     (or custom-title
         [rn/view
          [rn/text {:style tst/toolbar-title-text
                    :font  :toolbar-title}
           title]]))])

(defn- toggle-search-fn [text]
  (rf/dispatch [:set-in [:toolbar-search :show] text])
  (rf/dispatch [:set-in [:toolbar-search :text] ""]))

(defn- search-actions [show-search? search-text search-key actions]
  (if show-search?
    (if (pos? (count search-text))
      [(act/close #(do
                     (.clear @search-text-input)
                     (rf/dispatch [:set-in [:toolbar-search :text] ""])))]
      [act/search-icon])
    (into [(act/search #(toggle-search-fn search-key))] actions)))


(defn toolbar-with-search [{:keys [show-search?
                                   search-text
                                   search-key
                                   nav-action
                                   style
                                   modal?]
                            :as   opts}]
  ;; TODO(jeluard) refactor to components? Drop modal? and nav-action support
  [toolbar {:modal? modal?
             :style style}
   [nav-button
    (if show-search?
     (act/back #(toggle-search-fn nil))
     (or nav-action (if modal? act/default-close act/default-back)))]
   [toolbar-with-search-content opts]
   [actions (search-actions show-search? search-text search-key (:actions opts))]])