(ns messenger.android.chat
  (:require-macros
   [natal-shell.components :refer [view text image touchable-highlight list-view
                                   text-input toolbar-android]]
   [natal-shell.data-source :refer [data-source clone-with-rows]]
   [natal-shell.core :refer [with-error-view]]
   [natal-shell.alert :refer [alert]])
  (:require [om.next :as om :refer-macros [defui]]
            [re-natal.support :as sup]
            [messenger.state :as state]
            [messenger.android.resources :as res]))

(set! js/InvertibleScrollView (js/require "react-native-invertible-scroll-view"))

(defn nav-pop [nav]
  (binding [state/*nav-render* false]
    (.pop nav)))

(defui Message
  static om/Ident
  (ident [this {:keys [id]}]
         [:message/by-id id])
  static om/IQuery
  (query [this]
         '[:id :type :body :outgoing :delivery-status :date :new-day])
  Object
  (render
   [this]
   (let [{:keys [id body outgoing delivery-status date new-day] :as props}
         (om/props this)
         type (keyword (:type props))]
     (view {:paddingHorizontal 15}
           ;;; date
           (when new-day
             (text {:style {:marginVertical 10
                            :fontFamily "Avenir-Roman"
                            :fontSize 11
                            :color "#AAB2B2"
                            :letterSpacing 1
                            :lineHeight 15
                            :textAlign "center"
                            :opacity 0.8}}
                   date))
           ;;; body
           (view {:style (merge {:flexDirection "column"
                                 :width 260
                                 :marginVertical 5}
                                (if outgoing
                                  {:alignSelf "flex-end"
                                   :alignItems "flex-end"}
                                  {:alignSelf "flex-start"
                                   :alignItems "flex-start"}))}
                 (view {:style (merge {:borderRadius 6}
                                      (if (= type :text)
                                        {:paddingVertical 12
                                         :paddingHorizontal 16}
                                        {:paddingVertical 14
                                         :paddingHorizontal 10})
                                      (if outgoing
                                        {:backgroundColor "#D3EEEF"}
                                        {:backgroundColor "#FBF6E3"}))}
                       (if (= type :text)
                         (text {:style {:fontSize 14
                                        :fontFamily "Avenir-Roman"
                                        :color "#4A5258"}}
                               body)
                         ;;; audio
                         (view {:style {:flexDirection "row"
                                        :alignItems "center"}}
                               (view {:style {:width 33
                                              :height 33
                                              :borderRadius 50
                                              :elevation 1}}
                                     (image {:source res/play
                                             :style {:width 33
                                                     :height 33}}))
                               (view {:style {:marginTop 10
                                              :marginLeft 10
                                              :width 120
                                              :height 26
                                              :elevation 1}}
                                     (view {:style {:position "absolute"
                                                    :top 4
                                                    :width 120
                                                    :height 2
                                                    :backgroundColor "#EC7262"}})
                                     (view {:style {:position "absolute"
                                                    :left 0
                                                    :top 0
                                                    :width 2
                                                    :height 10
                                                    :backgroundColor "#4A5258"}})
                                     (text {:style {:position "absolute"
                                                    :left 1
                                                    :top 11
                                                    :fontFamily "Avenir-Roman"
                                                    :fontSize 11
                                                    :color "#4A5258"
                                                    :letterSpacing 1
                                                    :lineHeight 15}}
                                           "03:39")))))
                 ;;; delivery status
                 (when (and outgoing delivery-status)
                   (view {:style {:flexDirection "row"
                                  :marginTop 2}}
                         (image {:source (if (= (keyword delivery-status) :seen)
                                           res/seen-icon
                                           res/delivered-icon)
                                 :style {:marginTop 6
                                         :opacity 0.6}})
                         (text {:style {:fontFamily "Avenir-Roman"
                                        :fontSize 11
                                        :color "#AAB2B2"
                                        :opacity 0.8
                                        :marginLeft 5}}
                               (if (= (keyword delivery-status) :seen)
                                 "Seen"
                                 "Delivered")))))))))

(def message (om/factory Message {:keyfn :id}))

(defn render-row [row section-id row-id]
  (message (js->clj row :keywordize-keys true)))

(defn generate-message [n]
  {:id n
   :type (if (= (rem n 4) 3)
           :audio
           :text)
   :body (if (= (rem n 3) 0)
           (apply str n "." (repeat 5 " This is a text."))
           (str n ". This is a text."))
   :outgoing (< (rand) 0.5)
   :delivery-status (if (< (rand) 0.5) :delivered :seen)
   :date "TODAY"
   :new-day (= (rem n 3) 0)})

(defn generate-messages [n]
  (map generate-message (range 1 (inc n))))

(defn load-messages []
  (clone-with-rows (data-source {:rowHasChanged (fn [row1 row2]
                                                  (not= row1 row2))})
                   (vec (generate-messages 100))))

(defui NewMessage
  Object
  (render
   [this]
   (view {:style {:flexDirection "row"
                  :margin 10
                  :height 40
                  :backgroundColor "#E5F5F6"
                  :borderRadius 5}}
         (image {:source res/mic
                 :style {:marginTop 11
                         :marginLeft 14
                         :width 13
                         :height 20}})
         (text-input {:underlineColorAndroid "#9CBFC0"
                      :style {:flex 1
                              :marginLeft 18
                              :lineHeight 42
                              :fontSize 14
                              :fontFamily "Avenir-Roman"
                              :color "#9CBFC0"}}
                     "Your message")
         (image {:source res/smile
                 :style {:marginTop 11
                         :marginRight 12
                         :width 18
                         :height 18}})
         (image {:source res/att
                 :style {:marginTop 14
                         :marginRight 16
                         :width 17
                         :height 14}}))))

(def new-message (om/factory NewMessage))

(defui Chat
  Object
  (render
   [this]
   (let [{:keys [nav]} (om/get-computed this)
         messages-ds (load-messages)]
     (view {:style {:flex 1
                    :backgroundColor "white"}}
           (toolbar-android {:logo res/logo-icon
                             :title "Chat name"
                             :titleColor "#4A5258"
                             :subtitle "Last seen just now"
                             :subtitleColor "#AAB2B2"
                             :navIcon res/nav-back-icon
                             :style {:backgroundColor "white"
                                     :height 56
                                     :elevation 2}
                             :onIconClicked (fn []
                                              (nav-pop nav))})
           (list-view {:dataSource messages-ds
                       :renderScrollComponent
                       (fn [props]
                         (js/React.createElement js/InvertibleScrollView
                                                 (clj->js (merge (js->clj props)
                                                                 {:inverted true}))))
                       :renderRow render-row
                       :style {:backgroundColor "white"}})
           (new-message)))))

(def chat (om/factory Chat))
