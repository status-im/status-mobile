import pytest

from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView
from datetime import datetime
from tests.users import basic_user
import time
import base64


class TestPerformance(SingleDeviceTestCase):

    def get_timestamps_by_event(self, *args):
        # earlier event will be overwritten by latest in case of multiple events in a logcat!

        timestamps_by_event = dict()
        logcat = self.driver.get_log('logcat')
        for event in args:
            for line in logcat:
                if event in line['message']:
                    timestamps_by_event[event] = line['timestamp']
        return timestamps_by_event

    @marks.testrail_id(6216)
    @marks.skip
    def test_time_to_load_sign_in_screen(self):

        app_started = ':init/app-started'
        login_shown = ':on-will-focus :login'
        password_submitted = ':multiaccounts.login.ui/password-input-submitted'
        login_success = ':multiaccounts.login.callback/login-success'

        sign_in = SignInView(self.driver)
        sign_in.create_user()
        profile = sign_in.profile_button.click()
        profile.logout()
        home = sign_in.sign_in()
        home.plus_button.click()
        self.driver.info("Close app")
        self.driver.close_app()
        self.driver.info("Launch app")
        self.driver.launch_app()
        time.sleep(5)

        timestamps_by_event = self.get_timestamps_by_event(app_started, login_shown, password_submitted, login_success)
        for event in app_started, login_shown, password_submitted, login_success:
            self.driver.info("event: '%s' | timestamp: '%s' | time: '%s'" % (event, timestamps_by_event[event],
                                                                             datetime.utcfromtimestamp(
                                                                                 timestamps_by_event[event] / 1000)))

        time_to_login = (timestamps_by_event[login_success] - timestamps_by_event[password_submitted]) / 1000
        self.driver.info("Time to login is '%s'" % time_to_login)

        time_to_start_app = (timestamps_by_event[login_shown] - timestamps_by_event[app_started]) / 1000
        self.driver.info("Time to start the app is '%s'" % time_to_start_app)

        baseline_start_app = 0.8
        baseline_login = 1.2

        if time_to_start_app > baseline_start_app:
            self.errors.append(
                "time between starting the app and login screen is '%s' seconds, while baseline is '%s'!"
                % (time_to_start_app, baseline_start_app))
        if time_to_login > baseline_login:
            self.errors.append(
                "time between submitting a password and successful login is '%s' seconds, while baseline is '%s'!"
                % (time_to_login, baseline_login))
        self.errors.verify_no_errors()

    @marks.skip
    def test_genarate_db(self):
        sign_in = SignInView(self.driver)
        receiver = '0x0467d6ea67b28e017a33aa5d2dd627ce42180f9727e4fb33c8ac813cc9c2d2bf557ba245d70077c06ef31efe5cbb773244ee5fcc1ec89d2eab1b4922ec09babdbe'
        sender_passphrase = 'high design enjoy small coconut notable corn quantum blind dish green glow'
        home = sign_in.recover_access(sender_passphrase)
        profile = home.profile_button.click()
        profile.advanced_button.click()
        profile.fleet_setting_button.click()
        changed_fleet = 'eth.prod'
        profile.element_by_text(changed_fleet).click_until_presence_of_element(profile.confirm_button)
        profile.confirm_button.click()
        sign_in.sign_in()
        long_message = '4096 Comunidade das Nações[1][2][3][4] (em inglês: Commonwealth of Nations, ou simplesmente ' \
                       'the Commonwealth[5] , "a Comunidade"), originalmente criada como Comunidade Britânica de Nações ' \
                       '(em inglês: British Commonwealth of Nations),[6] é uma organização intergovernamental composta por ' \
                       '53 países membros independentes. Todas as nações membros da organização, com exceção de Moçambique ' \
                       '(antiga colónia do Império Português) e Ruanda (antiga colónia dos impérios Alemão e Belga), faziam ' \
                       'parte do Império Britânico, do qual se separaram.[7] Os Estados-membros cooperam num quadro ' \
                       'de valores e objetivos comuns, conforme descrito na Declaração de Singapura. Estes incluem ' \
                       'a promoção da democracia, direitos humanos, boa governança, Estado de Direito, liberdade ' \
                       'individual, igualitarismo, livre comércio, multilateralismo e a paz mundial.[8] A ' \
                       'Commonwealth não é uma união política, mas uma organização intergovernamental através da qual' \
                       ' os países com diversas origens sociais, políticas e econômicas são considerados como iguais' \
                       ' em status. As atividades da Commonwealth são realizadas através do permanente Secretariado ' \
                       'da Commonwealth, chefiado pelo Secretário-Geral, e por reuniões bienais entre os Chefes de ' \
                       'Governo da Commonwealth. O símbolo da sua associação livre é o chefe da Commonwealth, que é ' \
                       'uma posição cerimonial atualmente ocupada pela rainha Isabel II. Isabel II é também a monarca, ' \
                       'separada e independentemente, de 16 membros da Commonwealth, que são conhecidos como os ' \
                       '"reinos da Commonwealth". A Commonwealth é um fórum para uma série de organizações não ' \
                       'governamentais, conhecidas coletivamente como a "família da Commonwealth", promovidas através ' \
                       'da intergovernamental Fundação Commonwealth. Os Jogos da Commonwealth, a atividade mais visível ' \
                       'da organização, são um produto de uma dessas entidades. Estas organizações fortalecem a cultura ' \
                       'compartilhada da Commonwealth, que se estende através do esporte comum, patrimônio literário e ' \
                       'práticas políticas e jurídicas. Devido a isso, os países da Commonwealth não são considerados ' \
                       '"estrangeiros" uns aos outros. Refletindo esta missão, missões diplomáticas entre os países da ' \
                       'Commonwealth são designadas como Altas Comissões, em vez de embaixadas. Índice Os primeiros-ministros' \
                       ' de cinco membros da Commonwealth de 1944 em uma Conferência da Commonwealth Em 1884, ao visitar' \
                       ' a Austrália, Lord Rosebery descreveu que o Império Britânico estava a mudar, após algumas de suas ' \
                       'colónias tornaram-se mais independentes.[9] As conferências dos britânicos e das suas colónias ' \
                       'ocorriam periodicamente, desde a primeiro em 1887, levando à criação das conferências imperiais em ' \
                       '1911.[10] A proposta concreta foi apresentada por Jan Christian Smuts em 1917 quando ele cunhou o termo ' \
                       '"Comunidade Britânica das Nações", e previu o "futuro das relações constitucionais e reajustes ' \
                       'no Império Britânico".[11] Smuts argumentou com sucesso que o império deve ser representado ' \
                       'na Conferência de Versalhes por delegados das colónias, assim como a Grã-Bretanha.[12][13]' \
                       ' Na Declaração de Balfour na Conferência Imperial de 1926, a Grã-Bretanha e os seus domínios ' \
                       'concordaram que eles eram "iguais em status, em que ninguém os subordinava em qualquer aspecto ' \
                       'de seus assuntos internos ou externos, embora unidos pela fidelidade comum à Coroa, e livremente ' \
                       'associados como membros da Comunidade Britânica de Nações". Estes aspectos da relação foram ' \
                       'finalmente formalizada pelo Estatuto de Westminster em 1931. O estatuto foi aplicado ao Canadá ' \
                       'sem a necessidade de ratificação, entretanto, a Austrália, Nova Zelândia, e Terra Nova tinham que ' \
                       'ratificar o estatuto para que ela tivesse efeito. A atual província canadiana da Terra Nova ' \
                       '(Newfoundland) nunca retificou o estatuto, e em 16 de fevereiro de 1934, com o consentimento ' \
                       'do seu parlamento, o governo de Newfoundland voluntariamente deixou a organização. Newfoundland, ' \
                       'então, mais tarde tornou-se a décima província do Canadá, em 1949.[14] Austrália ratientão, mais tarde ' \
                       'tornou-se a décima província do Canadá, em 1949.[14] Austrália rati então, mais tarde tornou-se a décima' \
                       ' província do Canadá, '
        gh_link_message = 'https://github.com/status-im/status-react/issues'
        gif = 'https://media.giphy.com/media/7GYHmjk6vlqY8/giphy.gif'
        long_arabic_text = 'لك التفاصيل لتكتشف حقيقة وأساس تلك السعادة البشرية، فلا أحد يرفض أو يكره أو يتجنب ا' \
                           'لشعور بالسعادة، ولكن بفضل هؤلاء الأشخاص الذين لا يدركون بأن السعادة لا بد أن نستشعرها بصورة أكثر' \
                           ' عقلانية ومنطقية فيعرضهم هذا لمواجهة الظروف الأليمة، وأكرر بأنه لا يوجد من يرغب في الحب ونيل ال' \
                           'منال ويتلذذ بالآلام، الألم هو الألم ولكن نتيجة لظروف ما قد تكمن السعاده فيما نتحمله من كد وأسي.'
        text_emoji = '😀 😃 😄 😁 😆 😅 😂 🤣 🥲 ☺️ 😊 😇 🙂 🙃 😉 😌 😍 🥰 😘 😗 😙 😚 😋 😛 😝 😜 🤪 🤨 🧐 🤓 😎 🥸 ' \
                '🤩 🥳 😏 😒 😞 😔 😟 😕 🙁 ☹️ 😣 😖 😫 😩 🥺 😢 😭 😤 😠 😡 🤬 🤯 😳 🥵 🥶 😱 😨 😰 😥 😓 🤗 🤔 ' \
                '🤭 🤫 🤥 😶 😐 😑 😬 🙄 😯 😦 😧 😮 😲 🥱 😴 🤤 😪 😵 🤐 🥴 🤢 🤮 🤧 😷 🤒 🤕 🤑 🤠 😈 👿 👹 👺 ' \
                '🤡 💩 👻 💀 ☠️ 👽 👾 🤖 🎃 😺 😸 😹 😻 😼 😽 🙀 😿 😾'
        emoji = '😺'
        chinese_text = '吉伊杰杰勒! 艾 诶开 艾艾勒开 西吉艾艾诶.... 娜勒开伊哦吉艾艾弗 诶比勒屁哦 西吉艾艾伊'
        base_message = text_emoji
        chat = home.add_contact(receiver)
        base_message = long_arabic_text
        for n in range(14116, 16500):
            self.driver.info('Sending %s message' % str(n))
            chat.send_message('%s message \n'
                              'with several \n'
                              'lines' % str(n))
            if (n/100).is_integer():
                chat.send_message("**Another 100 messages are sent**")
            if (n/500).is_integer():
                chat.send_message("__IT IS ALREADY %s IN THIS CHAT__" % str(n))

        chat.send_message("Below are 1000 emoji cats")
        base_message = emoji
        for n in range(6000, 7000):
            self.driver.info('Sending %s message' % str(n))
            chat.send_message('%s' % base_message)
        chat.send_message("Wohooo 7200 messages!")
