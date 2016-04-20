package nudt.ceaserz;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

public class Test {

	public static void mysqlQuery() throws Exception {
		String url = "jdbc:mysql://223.255.18.169:3306/gdkj";
		String username = "nudt";
		String password = "bjgdFrist666";
		String sql = "select c.id,c.question_title,c.question_content,c.quick_answer,c.answer_content,c.is_best_answer from (select * from view_bdzd limit 100000) as c inner join (select a.id,count(*) as count from (select * from view_bdzd limit 100000) as a group by a.id having count >3) as b on c.id=b.id";
		ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = (Connection) DriverManager.getConnection(url, username, password);
			PreparedStatement pstmt = (PreparedStatement) con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			filterQuery(rs);
			rs.close();
			pstmt.close();
			con.close();
		} catch (ClassNotFoundException e) {
			System.out.println("找不到驱动程序类 ，加载驱动失败！");
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static String cleanString(String str) {
		str = (str == null) ? "" : str;
		String string = str.replaceAll("\\s+", "").replaceAll("[0-9]+", "0").replaceAll("[a-zA-Z]+", "a")
				.replaceAll("[^0a\u4e00-\u9fa5]+", ",");
		return string;

	}

	public static String segmentNlp(String str) throws Exception {
		String list = "";
		List<Term> termList = HanLP.segment(str);
		Pattern pattern = Pattern.compile("[\\pP|\\pS]+");
		int count = 0;
		for (Term term : termList) {
			String word = term.word;
			Matcher matcher = pattern.matcher(word);
			if (!matcher.matches() && count < 70) {
				list = list + word + " ";
				count += 1;
			}
		}
		return list.trim();
	}

	public static void filterQuery(ResultSet rs) throws Exception {
		while (rs.next()) {
			String question_title = cleanString(rs.getString("question_title"));
			String question_content = cleanString(rs.getString("question_content"));
			String quick_answer = cleanString(rs.getString("quick_answer"));
			String answer_content = cleanString(rs.getString("answer_content"));
			String is_best_answer = rs.getString("is_best_answer");
			System.out.println("is_best_answer is :" + is_best_answer);
			FileUtils.write(new File("/home/ceaserz/Desktop/lzz/cnn1"),
					segmentNlp(question_title + "," + question_content) + "\n", "utf-8", true);
			FileUtils.write(new File("/home/ceaserz/Desktop/lzz/lstm1"),
					segmentNlp(quick_answer + "," + answer_content) + "\n", "utf-8", true);
			if ("false".equals(is_best_answer)) {
				FileUtils.write(new File("/home/ceaserz/Desktop/lzz/cat1"), "0" + "\n", "utf-8", true);
			} else {
				FileUtils.write(new File("/home/ceaserz/Desktop/lzz/cat1"), "1" + "\n", "utf-8", true);
			}
		}
	}

	public static void main(String[] args) throws Exception {

		mysqlQuery();
	}

}
