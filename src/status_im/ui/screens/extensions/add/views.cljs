(ns status-im.ui.screens.extensions.add.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [clojure.string :as string]
            [status-im.extensions.core :as extensions]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.screens.extensions.add.styles :as styles]))

(defn cartouche [{:keys [header]} content]
  [react/view {:style styles/cartouche-container}
   [react/text {:style styles/cartouche-header}
    header]
   [react/view {:style styles/cartouche-content-wrapper}
    [react/view {:flex 1}
     content]]])

(defn hooks [{:keys [hooks]}]
  (mapcat (fn [[hook-id values]]
            (map (fn [[id]]
                   (str (name hook-id) "." (name id)))
                 values))
          hooks))

(views/defview show-extension-base [modal?]
  (views/letsubs [{:keys [extension-data url]} [:get-staged-extension]]
    (let [{:keys [data errors]} extension-data]
      (if data
        [react/view styles/screen
         [status-bar/status-bar]
         [react/keyboard-avoiding-view components.styles/flex
          [toolbar/simple-toolbar (i18n/label :t/extension) modal?]
          [react/scroll-view {:keyboard-should-persist-taps :handled}
           [react/view styles/wrapper
            [react/view {:style {:border-radius 8 :margin 10 :padding 8 :background-color colors/red}}
             [react/text {:style {:color colors/white}}
              (i18n/label :t/extensions-disclaimer)]]
            [cartouche {:header (i18n/label :t/identifier)}
             [react/text {:style styles/text}
              (str (get-in data ['meta :name]))]]
            [cartouche {:header (i18n/label :t/name)}
             [react/text {:style styles/text}
              (str (get-in data ['meta :name]))]]
            [cartouche {:header (i18n/label :t/description)}
             [react/text {:style styles/text}
              (str (get-in data ['meta :description]))]]
            [cartouche {:header (i18n/label :t/hooks)}
             (into [react/view] (for [hook (hooks data)]
                                  [react/text {:style styles/text}
                                   (str hook)]))]
            [cartouche {:header (i18n/label :t/permissions)}
             [react/text {:style styles/text}
              (i18n/label :t/none)]]
            [cartouche {:header (i18n/label :t/errors)}
             (if errors
               (into [react/view] (for [error errors]
                                    [react/text {:style styles/text}
                                     (str (name (:pluto.reader.errors/type error)) " " (str (:pluto.reader.errors/value error)))]))
               [react/text {:style styles/text}
                (i18n/label :t/none)])]]]
          [react/view styles/bottom-container
           [react/view components.styles/flex]
           [components.common/bottom-button
            {:forward?  true
             :label     (i18n/label :t/install)
             :disabled? (not (empty? errors))
             :on-press  #(re-frame/dispatch [:extensions.ui/install-button-pressed url data modal?])}]]]]
        [react/view styles/screen
         [status-bar/status-bar]
         [react/view {:flex 1}
          [toolbar/simple-toolbar (i18n/label :t/extension)]
          [react/view {:style {:flex 1 :justify-content :center :align-items :center}}
           [react/text (i18n/label :t/invalid-extension)]
           [react/text (str errors)]]]]))))

(views/defview show-extension []
  [show-extension-base false])

(views/defview show-extension-modal []
  [show-extension-base true])

(def qr-code
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:qr-scanner.ui/scan-qr-code-pressed
                                                             {:toolbar-title (i18n/label :t/scan-qr)}
                                                             :extensions.callback/qr-code-scanned])
                              :style    styles/qr-code}
   [react/view
    [vector-icons/icon :main-icons/qr {:color colors/blue}]]])

(views/defview edit-extension []
  (views/letsubs [manage-extension [:get-manage-extension]]
    (let [url     (get-in manage-extension [:url :value])
          pressed (reagent/atom false)]
      [react/view styles/screen
       [status-bar/status-bar]
       [react/keyboard-avoiding-view components.styles/flex
        [toolbar/simple-toolbar (i18n/label :t/extension-find)]
        [react/scroll-view {:keyboard-should-persist-taps :handled}
         [react/view styles/wrapper
          [text-input/text-input-with-label
           {:label          (i18n/label :t/extension-address)
            :style          styles/input
            :container      styles/input-container
            :content        qr-code
            :default-value  url
            :placeholder    (i18n/label :t/extension-url)
            :on-change-text #(re-frame/dispatch [:extensions.ui/input-changed :url %])}]]]
        [react/view styles/bottom-container
         [react/view components.styles/flex]
         [components.common/bottom-button
          {:forward?  true
           :label     (i18n/label :t/find)
           :disabled? (and (not @pressed) (not (extensions/valid-uri? url)))
           :on-press  #(do (reset! pressed true) (re-frame/dispatch [:extensions.ui/find-button-pressed (string/trim url)]))}]]]])))
