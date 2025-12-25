using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace SQLViewer
{
    public partial class UserControl1 : UserControl
    {
        public string CurrentServer { get; private set; }
        public string CurrentDatabase { get; private set; }
        public string CurrentTable { get; private set; }

        public UserControl1(string serverName, string dbName, string tbName)
        {
            InitializeComponent();
            CurrentServer = serverName;
            CurrentDatabase = dbName;
            CurrentTable = tbName;

        }

        private void UserControl1_Load(object sender, EventArgs e)
        {
            initDataGridView();

            //查询表结构
            string tableDDL = HttpUtils.showTable(CurrentDatabase, CurrentTable);
            richTextBox1.Text = tableDDL;

            //初始化排序与查询条件
            lastSortColumn = "";
            lastSortAsc = true;
            textBox1.Text = "";
            label5.Text ="服务:"+CurrentServer;


            //查询数据
            handleQuery();

        }

        private void initDataGridView()
        {
            //优化dataGridView
            dataGridView1.GetType().InvokeMember(
                "DoubleBuffered",
                System.Reflection.BindingFlags.NonPublic |
                System.Reflection.BindingFlags.Instance |
                System.Reflection.BindingFlags.SetProperty,
                null,
                dataGridView1,
                new object[] { true }
            );
            dataGridView1.AlternatingRowsDefaultCellStyle = null;
            dataGridView1.AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.AllCells;
            dataGridView1.RowHeadersWidthSizeMode = DataGridViewRowHeadersWidthSizeMode.DisableResizing;
            dataGridView1.ColumnHeadersHeightSizeMode = DataGridViewColumnHeadersHeightSizeMode.DisableResizing;
            //dataGridView1.AllowUserToResizeRows = false;
            //dataGridView1.AllowUserToResizeColumns = false;
            dataGridView1.ReadOnly = true; // 如果不编辑数据
            dataGridView1.VirtualMode = true; // 如果数据大量（>1000）
            dataGridView1.AllowUserToAddRows = false;
            dataGridView1.Font = new Font("Microsoft YaHei UI", 12);
            dataGridView1.ColumnHeadersHeight = 36;
            dataGridView1.RowTemplate.Height = 30;

        }

        private void button2_Click(object sender, EventArgs e)
        {
            richTextBox1.Visible = !richTextBox1.Visible;
        }

        private void button3_Click(object sender, EventArgs e)
        {
            button3.Enabled = false;
            button3.Text = "查询中...";
            handleQuery();
            button3.Enabled = true;
            button3.Text = "查询";
        }

        public void handleQuery()
        {
            //组装sql
            string orderBy = "";
            if (lastSortColumn != null && !lastSortColumn.Trim().Equals(""))
            {
                orderBy = lastSortColumn + (lastSortAsc ? " DESC" : "");
            }
            string sql = buildSql(textBox1.Text, orderBy);
            label1.Text = sql;
            JObject jo = HttpUtils.querySql(CurrentDatabase, CurrentTable, sql);
            int status = (int)jo["status"];
            JObject data = (JObject)jo["data"];
            string msg = (string)jo["msg"];
            if (status != 0)
            {
                MessageBox.Show(msg, "错误", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            else
            {
                bindJsonToDataGridView(data);
            }
        }

        public string buildSql(string condition, string orderBy)
        {
            string sql = "select * from " + CurrentTable;
            if (condition != null && !condition.Trim().Equals(""))
            {
                sql += (" where " + condition);
            }
            if (orderBy != null && !orderBy.Trim().Equals(""))
            {
                sql += (" order by " + orderBy);
            }
            return sql;
        }

        public void bindJsonToDataGridView(JObject data)
        {
            double query_time = (double)data["query_time"];
            int affected_rows = (int)data["affected_rows"];

            label2.Text = "查询时间：" + query_time;
            label3.Text = "条数：" + affected_rows;

            JArray column_list = (JArray)data["column_list"];
            JArray rows = (JArray)data["rows"];

            // 创建 DataTable
            DataTable dt = new DataTable();

            foreach (var col in column_list)
            {
                dt.Columns.Add(col.ToString());
            }

            if (affected_rows > 0)
            {
                // 遍历每行数据
                foreach (JArray row in rows)
                {
                    DataRow dr = dt.NewRow();
                    for (int i = 0; i < column_list.Count; i++)
                    {
                        dr[i] = row[i].ToString();
                    }
                    dt.Rows.Add(dr);
                }
            }

            dataGridView1.DataSource = null;
            dataGridView1.DataSource = dt;

        }

        // 保存上一列的排序方向
        private string lastSortColumn = "";
        private bool lastSortAsc = true;

        private void dataGridView1_ColumnHeaderMouseClick(object sender, DataGridViewCellMouseEventArgs e)
        {
            string colName = dataGridView1.Columns[e.ColumnIndex].Name;

            // 切换排序方向
            if (colName == lastSortColumn)
                lastSortAsc = !lastSortAsc;
            else
            {
                lastSortColumn = colName;
                lastSortAsc = true;
            }

            handleQuery();

            // 显示排序图标（三角形）
            dataGridView1.Columns[colName].HeaderCell.SortGlyphDirection =
                lastSortAsc ? SortOrder.Descending : SortOrder.Ascending;
        }

        private void button5_Click(object sender, EventArgs e)
        {
            button5.Enabled = false;
            button5.Text = "查询中...";
            int count = HttpUtils.countBySql(CurrentDatabase, CurrentTable);
            label4.Text = "总数：" + count;
            button5.Enabled = true;
            button5.Text = "计算总数";
        }

        private void button4_Click(object sender, EventArgs e)
        {
            lastSortColumn = "";
            lastSortAsc = true;
            handleQuery();
        }

        private const int MAX_COLUMN_WIDTH = 500;

        private void dataGridView1_DataBindingComplete(object sender, DataGridViewBindingCompleteEventArgs e)
        {
            if (lastSortColumn != null && !lastSortColumn.Trim().Equals(""))
            {
                // 显示排序图标（三角形）
                dataGridView1.Columns[lastSortColumn].HeaderCell.SortGlyphDirection =
                lastSortAsc ? SortOrder.Descending : SortOrder.Ascending;
            }

            foreach (DataGridViewColumn col in dataGridView1.Columns)
            {
                col.SortMode = DataGridViewColumnSortMode.Programmatic;
                if (col.Width > MAX_COLUMN_WIDTH)
                {
                    col.Width = MAX_COLUMN_WIDTH;
                    col.AutoSizeMode = DataGridViewAutoSizeColumnMode.None;
                }
            }
        }

        private void button6_Click(object sender, EventArgs e)
        {
            if (!string.IsNullOrEmpty(label1.Text))
            {
                Clipboard.SetText(label1.Text); // 将内容复制到剪切板
            }
        }
    }
}
