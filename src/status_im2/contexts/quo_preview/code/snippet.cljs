(ns status-im2.contexts.quo-preview.code.snippet
  (:require [quo2.components.code.snippet :as snippet]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def go-example
  "func (s *Server) listenAndServe() {
    cfg := &tls.Config{Certificates: []tls.Certificate{*s.cert}, ServerName: s.hostname, MinVersion: tls.VersionTLS12}

    // in case of restart, we should use the same port as the first start in order not to break existing links
    listener, err := tls.Listen(\"tcp\", s.getHost(), cfg)
    if err != nil {
        s.logger.Error(\"failed to start server, retrying\", zap.Error(err))
        s.ResetPort()
        err = s.Start()
        if err != nil {
            s.logger.Error(\"server start failed, giving up\", zap.Error(err))
        }
        return
    }

    err = s.SetPort(listener.Addr().(*net.TCPAddr).Port)
    if err != nil {
        s.logger.Error(\"failed to set Server.port\", zap.Error(err))
        return
    }

    if s.afterPortChanged != nil {
        s.afterPortChanged(s.port)
    }
    s.run = true

    err = s.server.Serve(listener)
    if err != http.ErrServerClosed {
        s.logger.Error(\"server failed unexpectedly, restarting\", zap.Error(err))
        err = s.Start()
        if err != nil {
            s.logger.Error(\"server start failed, giving up\", zap.Error(err))
        }
        return
    }

    s.run = false
}")

(def clojure-example
  "(defn request->xhrio-options
  [{:as   request
    :keys [on-success on-failure]
    :or   {on-success      [:http-no-on-success]
           on-failure      [:http-no-on-failure]}}]
  ; wrap events in cljs-ajax callback
  (let [api (new goog.net.XhrIo)]
    (-> request
        (assoc
          :api     api
          :handler (partial ajax-xhrio-handler
                            #(dispatch (conj on-success %))
                            #(dispatch (conj on-failure %))
                            api))
        (dissoc :on-success :on-failure :on-request))))")

(def examples
  {:clojure {:language :clojure
             :text     clojure-example}
   :go      {:language :go
             :text     go-example}})

(def descriptor
  [{:label   "Language:"
    :key     :language
    :type    :select
    :options [{:key   :clojure
               :value :clojure}
              {:key   :go
               :value :go}]}
   {:label   "Max lines:"
    :key     :max-lines
    :type    :select
    :options (map (fn [n]
                    {:key   n
                     :value (str n " lines")})
                  (range 0 41 5))}
   {:label "Syntax highlight:"
    :key   :syntax
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:language  :clojure
                             :max-lines 40
                             :syntax    true})]
    (fn []
      (let [language  (if (:syntax @state) (:language @state) :text)
            text      (-> (:language @state) examples :text)
            max-lines (as-> (:max-lines @state) max-lines
                        (js/parseInt max-lines)
                        (when-not (js/Number.isNaN max-lines)
                          max-lines))]
        [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
         [rn/view {:style {:padding-bottom 150}}
          [preview/customizer state descriptor]
          [rn/view
           {:style {:padding-vertical   60
                    :padding-horizontal 16}}
           [snippet/snippet
            {:language      language
             :max-lines     max-lines
             :on-copy-press #(js/alert %)}
            text]]]]))))

(defn preview-code-snippet
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                      1
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])
