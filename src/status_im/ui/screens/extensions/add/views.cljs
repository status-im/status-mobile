(ns status-im.ui.screens.extensions.add.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.screens.extensions.add.styles :as styles]))

(defn cartouche [{:keys [header]}  content]
  [react/view {:style styles/cartouche-container}
   [react/text {:style styles/cartouche-header}
    header]
   [react/view {:style styles/cartouche-content-wrapper}
    [react/view {:flex 1}
     [react/text {:style styles/text}
      content]]]])

(defn hooks [{:keys [hooks]}]
  (mapcat (fn [[hook-id values]]
            (map (fn [[id]]
                   (symbol "hook" (str (name hook-id) "." (name id))))
                 values))
          hooks))

(views/defview show-extension []
  (views/letsubs [{:keys [data errors]} [:get-staged-extension]]
    [react/view components.styles/flex
     [status-bar/status-bar]
     [react/keyboard-avoiding-view components.styles/flex
      [toolbar/simple-toolbar (i18n/label :t/extension)]
      [react/scroll-view {:keyboard-should-persist-taps :handled}
       [react/view styles/wrapper
        [cartouche {:header (i18n/label :t/identifier)}
         (str (get-in data ['meta :name]))]
        [cartouche {:header (i18n/label :t/name)}
         (str (get-in data ['meta :name]))]
        [cartouche {:header (i18n/label :t/description)}
         (str (get-in data ['meta :description]))]
        [cartouche {:header (i18n/label :t/hooks)}
         (string/join " " (hooks data))]
        [cartouche {:header (i18n/label :t/permissions)}
         (i18n/label :t/none)]
        [cartouche {:header (i18n/label :t/errors)}
         (i18n/label :t/none)]]]
      [react/view styles/bottom-container
       [react/view components.styles/flex]
       [components.common/bottom-button
        {:forward?  true
         :label     (i18n/label :t/install)
         :disabled? (seq errors)
         :on-press  #(re-frame/dispatch [:extension/install data])}]]]]))

(views/defview add-extension []
  (views/letsubs [extension-url [:get-extension-url]]
    [react/view components.styles/flex
     [status-bar/status-bar]
     [react/keyboard-avoiding-view components.styles/flex
      [toolbar/simple-toolbar (i18n/label :t/extension-find)]
      [react/scroll-view {:keyboard-should-persist-taps :handled}
       [react/view styles/wrapper
        [text-input/text-input-with-label
         {:label           (i18n/label :t/extension-address)
          :style           styles/input
          :container       styles/input-container
          :placeholder     (i18n/label :t/extension-url)
          :on-change-text  #(re-frame/dispatch [:extension/edit-address %])}]]]
      [react/view styles/bottom-container
       [react/view components.styles/flex]
       [components.common/bottom-button
        {:forward?  true
         :label     (i18n/label :t/find)
         :disabled? (string/blank? extension-url)
         :on-press  #(re-frame/dispatch [:extension/show extension-url])}]]]]))
