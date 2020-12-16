
import groovy.swing.SwingBuilder
import groovy.beans.Bindable
import static javax.swing.JFrame.EXIT_ON_CLOSE
import java.awt.*
import org.jasypt.util.text.BasicTextEncryptor

@Bindable
class User {
	String userId, pw
	String toString() {
		"User[userid=$userId,pw=$pw]"
	}
}

class Crypt {
	static void encryptAndStore(User user) {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor()
		textEncryptor.setPassword("test")
		def encrypted =  textEncryptor.encrypt(user.pw)
		def config = new ConfigSlurper().parse(new File('src/main/jenkins/client/config.groovy').toURI().toURL())
		config.put("pw",encrypted)
		config.put("userId", user.userId)
		def fw = new FileWriter("src/main/jenkins/client/config.groovy",false)
		config.writeTo(fw)
		println "Written user data encrypted to Config"
	}
	
	static String decrypt(String input) {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor()
		textEncryptor.setPassword("test")
		return textEncryptor.decrypt(input)
	}
}

def user = new  User(userId: '', pw: '')

def swingBuilder = new SwingBuilder()
swingBuilder.edt {
	// edt method makes sure UI is build on Event Dispatch Thread.
	lookAndFeel 'nimbus'  // Simple change in look and feel.
	dialog(title: 'User', size: [350, 230],
	pack: true , show: true, locationRelativeTo: null,
	modal: true,
	alwaysOnTop: true,
	resizable: false) {
		borderLayout(vgap: 5)

		panel(constraints: BorderLayout.CENTER,
		border: compoundBorder([emptyBorder(10), titledBorder('Enter your userId / pw:')])) {
			tableLayout {
				tr {
					td { label 'Userid:'  // text property is default, so it is implicit.
					}
					td {
						textField user.userId, id: 'userIdField', columns: 20
					}
				}
				tr {
					td { label 'Password:' }
					td {
						passwordField id: 'pwField', columns: 20, text: user.pw
					}
				}
			}

		}

		panel(constraints: BorderLayout.SOUTH) {
			button text: 'Save', actionPerformed: { Crypt.encryptAndStore(user) }
			button text: 'Exit', actionPerformed: { 	dispose() }
			
		}

		bean user,
				userId: bind { userIdField.text },
				pw: bind { pwField.text }
			}
}